package com.letv.leui.common.recommend.net.parse;

import android.util.Log;
import com.letv.leui.common.recommend.utils.LogHelper;
import com.letv.leui.common.recommend.widget.adapter.dto.RecommendAllDTO;
import org.json.JSONObject;

/**
 * Created by dupengtao on 15-1-6.
 */
public class DefaultParser implements IBaseParser{
    private static final String TAG = "DefaultParser";
    private static DefaultParser ourInstance = new DefaultParser();

    public static DefaultParser getInstance() {
        return ourInstance;
    }

    private DefaultParser() {
    }

    @Override
    public <T> T toParse(String context, Class<T> clazz) {
        return readValue(context,clazz);
    }

    public  <T> T readValue(String context, Class<T> clazz) {
        try {
            if (RecommendAllDTO.class == clazz) {
                RecommendAllDTO info = DefaultParserUtils.parserRecommendAllDTO(new JSONObject(context));
                Log.i(TAG, "readValue");
                return (T) info;
            }
        }catch (Exception e){
            LogHelper.w(TAG, e.toString());
            return null;
        }
        return null;
    }

}
