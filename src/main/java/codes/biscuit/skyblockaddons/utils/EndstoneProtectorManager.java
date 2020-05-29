package codes.biscuit.skyblockaddons.utils;

import codes.biscuit.skyblockaddons.SkyblockAddons;
import lombok.Getter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntityIronGolem;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class EndstoneProtectorManager {

    @Getter private static boolean canDetectSkull = false;
    @Getter private static Stage minibossStage = null;
    @Getter private static int zealotCount = 0;

    private static long lastWaveStart = -1;

    public static void tick() {
        Minecraft mc = Minecraft.getMinecraft();
        SkyblockAddons main = SkyblockAddons.getInstance();

        if (mc.theWorld != null && (main.getUtils().getLocation() == Location.THE_END || main.getUtils().getLocation() == Location.DRAGONS_NEST) &&
                main.getConfigValues().isEnabled(Feature.ENDSTONE_PROTECTOR_DISPLAY)) {
            WorldClient worldClient = mc.theWorld;

            Chunk chunk = worldClient.getChunkFromBlockCoords(new BlockPos(-689, 5, -273)); // This is the original spawn.
            if (chunk == null || !chunk.isLoaded()) {
                canDetectSkull = false;
                return;
            }

            Stage stage = null;

            for (Entity entity : worldClient.loadedEntityList) {
                if (entity instanceof EntityIronGolem) {
                    stage = Stage.GOLEM_ALIVE;
                    break;
                }
            }

            if (stage == null) {
                stage = Stage.detectStage(worldClient);
            }

            canDetectSkull = true;

            if (minibossStage != stage) {
                int timeTaken = (int) (System.currentTimeMillis()-lastWaveStart);
                String previousStage = (minibossStage == null ? "null" : minibossStage.name());
                String newStage = stage.name();

                String zealotsKilled = "N/A";
                if (minibossStage != null) {
                    zealotsKilled = String.valueOf(zealotCount);
                }

                int totalSeconds = timeTaken/1000;
                int minutes = totalSeconds/60;
                int seconds = totalSeconds%60;

                main.getLogger().info("Endstone Protector stage updated from "+previousStage+" to "+newStage+". " +
                        "Your zealot kill count was "+zealotsKilled+". This took "+minutes+"m "+seconds+"s.");

                if (minibossStage == Stage.GOLEM_ALIVE && stage == Stage.NO_HEAD) {
                    zealotCount = 0;
                }

                minibossStage = stage;
                lastWaveStart = System.currentTimeMillis();
            }
        } else {
            canDetectSkull = false;
        }
    }

    public static void onKill() {
        zealotCount++;
    }

    public static void reset() {
        minibossStage = null;
        zealotCount = 0;
        canDetectSkull = false;
    }

    public enum Stage {
        NO_HEAD(-1),
        STAGE_1(0),
        STAGE_2(1),
        STAGE_3(2),
        STAGE_4(3),
        STAGE_5(4),
        GOLEM_ALIVE(-1);
        private int blocksUp;

        Stage(int blocksUp) {
            this.blocksUp = blocksUp;
        }

        private static Stage lastStage = null;
        private static BlockPos lastPos = null;

        public static Stage detectStage(WorldClient worldClient) {
            if (lastStage != null && lastPos != null) {
                if (Blocks.skull.equals(worldClient.getBlockState(lastPos).getBlock())) {
                    return lastStage;
                }
            }

            for (Stage stage : values()) {
                if (stage.blocksUp != -1) {
                    // These 4 coordinates are the bounds of the dragon's nest.
                    for (int x = -749; x < -602; x++) {
                        for (int z = -353; z < -202; z++) {
                            BlockPos blockPos = new BlockPos(x, 5+stage.blocksUp, z);
                            if (Blocks.skull.equals(worldClient.getBlockState(blockPos).getBlock())) {
                                lastStage = stage;
                                lastPos = blockPos;

                                return stage;
                            }
                        }
                    }
                }
            }
            lastStage = null;
            lastPos = null;
            return Stage.NO_HEAD;
        }
    }
}
