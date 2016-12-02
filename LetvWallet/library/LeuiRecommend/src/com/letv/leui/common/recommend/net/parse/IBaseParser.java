package com.letv.leui.common.recommend.net.parse;

/**
 * Created by dupengtao on 2014/6/16.
 */
public interface IBaseParser {

    <T> T toParse(String context, Class<T> clazz);
}
