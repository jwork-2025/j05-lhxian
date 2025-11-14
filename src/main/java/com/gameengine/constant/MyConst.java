package com.gameengine.constant;

public class MyConst {
    public static final int BLOCK_SIZE=32;
    public static final int COLUMN_BLOCK_CNT=36, ROW_BLOCK_CNT=28;
    public static final int WIDTH = COLUMN_BLOCK_CNT * BLOCK_SIZE, HEIGHT= ROW_BLOCK_CNT *BLOCK_SIZE;
    public static final int GRASS_MAX =32, GRASS_MIN =16, GRASS_SIZE =32,
        WATER_WIDTH =64, WATER_HEIGHT =32;
    /**
     * for enemy size;
     */
    public static final int ENEMY_WIDTH =16,ENEMY_HEIGHT =32;
    /**
     * for player size;
     */
    public static final int PLAYER_WIDTH =16, PLAYER_HEIGHT=32;

    public static final int TREE_WIDTH =32,TREE_HEIGHT=32;

    public static final int COIN_WIDTH =16, COIN_HEIGHT=16;
    public static final int STONE_WIDTH =32, STONE_HEIGHT=32;
    public static final int SPRING_WIDTH =48,SPRING_HEIGHT=48;

}
