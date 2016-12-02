package com.letv.walletbiz.movie.utils;

import com.letv.walletbiz.movie.beans.MovieSeatColumnInfo;
import com.letv.walletbiz.movie.beans.MovieSeatList;
import com.letv.walletbiz.movie.beans.MovieSeatRowInfo;
import com.letv.walletbiz.movie.beans.MovieSoldInfo;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by changjiajie on 16-2-18.
 */
public class MovieSeatDataManager implements Serializable {

    public static final int DEFAULTINDEX = -1;
    /**
     * 购买最大数量
     */
    private static int mMaxCount = 4;
    private List<MovieSoldInfo> mCheckedSeatList;

    private MovieSeatList mSeatInfosList;


    public MovieSeatDataManager() {
        this.mCheckedSeatList = new ArrayList<MovieSoldInfo>();
    }

    public void setMovieSeatList(MovieSeatList seatInfosList) {
        this.mSeatInfosList = seatInfosList;
        for (MovieSoldInfo checkSeatInfo : this.mCheckedSeatList) {
            this.mSeatInfosList.seat_info[checkSeatInfo.mRowNum - 1].detail[checkSeatInfo.mColumnNum - 1].state = MovieSeatColumnInfo.CHECKEDSTATE;
        }
    }

    public MovieSeatList getMovieSeatList() {
        return this.mSeatInfosList;
    }

    public List<MovieSoldInfo> getCheckedSeatList() {
        return this.mCheckedSeatList;
    }

    /**
     * 设置购买最大数量
     *
     * @param maxCount
     */
    public void setMaxSeatNum(int maxCount) {
        this.mMaxCount = maxCount;
    }


    /**
     * 获取最大购买数量
     *
     * @return
     */
    public int getMaxSeatNum() {
        return mMaxCount;
    }

    public void addCheckSeatInfo(MovieSoldInfo soldInfo) {
        if (mCheckedSeatList != null && !mCheckedSeatList.contains(soldInfo)) {
            this.mCheckedSeatList.add(soldInfo);
        }
    }

    public void deleteCheckSeatInfo(int index) {
        this.mCheckedSeatList.remove(index);
    }

    public int checkCheckedSeat(String area, int mRowNum, int mColumnNum) {
        int containsIndex = this.DEFAULTINDEX;
        int size = this.mCheckedSeatList.size();
        for (int i = 0; i < size; i++) {
            MovieSoldInfo soldInfo = this.mCheckedSeatList.get(i);
            if (soldInfo.area.equals(area) && soldInfo.mRowNum == mRowNum) {
                if (soldInfo.mColumnNum == mColumnNum) {
                    containsIndex = i;
                    break;
                }
            }
        }
        return containsIndex;
    }

    public boolean isCheckedMaxSeatNum() {
        if (this.mCheckedSeatList.size() >= this.mMaxCount) {
            return true;
        }
        return false;
    }

    public void mergeData(MovieSeatList seatInfosList, List<MovieSoldInfo> soldSeatList) {
        if (soldSeatList != null) {
            for (MovieSoldInfo soldInfo : soldSeatList) {
                //检索每一排
                for (MovieSeatRowInfo seatInfo : seatInfosList.seat_info) {
                    if (seatInfo.desc.equals("0")) {
                        //此处是过道区域
                        continue;
                    }
                    /**
                     * 等为微票erea 统一后使用
                     *if (seatInfo.area.equals(soldInfo.area) && seatInfo.desc.equals(soldInfo.desc)) {
                     */
                    if (seatInfo.desc.equals(soldInfo.desc)) {
                        for (String seatNum : soldInfo.seatNumList) {
                            //检索每一列
                            for (MovieSeatColumnInfo detailInfo : seatInfo.detail) {
                                if (detailInfo.n.equals(seatNum)) {
                                    detailInfo.state = MovieSeatColumnInfo.SOLDSEATSTATE;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public List<MovieSoldInfo> getSoldInfo(String soldData) {
        String lineSplitIdentify = "@";
        String infoSplitIdentify = ":";
        String seatSplitIdentify = ",";
        List<MovieSoldInfo> soldList = null;
        if (soldData != null && !soldData.equals("")) {
            soldList = new ArrayList<MovieSoldInfo>();
            String[] soldListStr = soldData.split(lineSplitIdentify);
            if (soldListStr.length > 0) {
                for (String soldInfoStr : soldListStr) {
                    MovieSoldInfo soldInfo = new MovieSoldInfo();
                    String[] infoListStr = soldInfoStr.split(infoSplitIdentify);
                    if (infoListStr.length > 0) {
                        soldInfo.area = infoListStr[0];
                        soldInfo.desc = infoListStr[1];
                        if (infoListStr[2] != null && !infoListStr[2].equals("")) {
                            String[] seatNumInfo = infoListStr[2].split(seatSplitIdentify);
                            if (seatNumInfo.length > 0) {
                                soldInfo.seatNumList = new ArrayList<String>();
                                for (String seatNum : seatNumInfo) {
                                    soldInfo.seatNumList.add(seatNum);
                                }
                            }
                        }
                        soldList.add(soldInfo);
                    }
                }
            }
        }
        return soldList;
    }
}
