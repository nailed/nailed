package jk_5.nailed.server.world;

import jk_5.nailed.api.world.DefaultWorldProviders;
import jk_5.nailed.api.world.Dimension;
import jk_5.nailed.api.world.WorldProvider;

import javax.annotation.Nonnull;

public class NailedDefaultWorldProviders implements DefaultWorldProviders {

    private static final NailedDefaultWorldProviders INSTANCE = new NailedDefaultWorldProviders();

    private abstract class IdTracked implements WorldProvider {

        private int id;

        @Override
        public final int getId() {
            return this.id;
        }

        @Override
        public final void setId(int id) {
            this.id = id;
        }

        @Nonnull
        @Override
        public Dimension getDimension() {
            return Dimension.OVERWORLD;
        }

        @Override
        public String getOptions() {
            return null;
        }
    }

    @Nonnull
    @Override
    public WorldProvider getVoidProvider() {
        return new IdTracked() {
            @Nonnull
            @Override
            public String getType() {
                return "void";
            }
        };
    }

    @Nonnull
    @Override
    public WorldProvider getOverworldProvider() {
        return new IdTracked() {
            @Nonnull
            @Override
            public String getType() {
                return "overworld";
            }
        };
    }

    @Nonnull
    @Override
    public WorldProvider getNetherProvider() {
        return new IdTracked() {
            @Nonnull
            @Override
            public String getType() {
                return "nether";
            }

            @Nonnull
            @Override
            public Dimension getDimension() {
                return Dimension.NETHER;
            }
        };
    }

    @Nonnull
    @Override
    public WorldProvider getEndProvider() {
        return new IdTracked() {
            @Nonnull
            @Override
            public String getType() {
                return "end";
            }

            @Nonnull
            @Override
            public Dimension getDimension() {
                return Dimension.END;
            }
        };
    }

    @Nonnull
    @Override
    public WorldProvider getFlatProvider(@Nonnull final String pattern) {
        return new IdTracked() {
            @Nonnull
            @Override
            public String getType() {
                return "flat";
            }

            @Override
            public String getOptions() {
                return pattern;
            }
        };
    }

    public static NailedDefaultWorldProviders instance(){
        return INSTANCE;
    }
}
