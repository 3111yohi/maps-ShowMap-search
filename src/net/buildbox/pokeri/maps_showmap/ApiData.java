package net.buildbox.pokeri.maps_showmap;

import java.io.InputStream;
import java.util.HashMap;

import android.util.Log;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import android.util.Xml;

public class ApiData {

	private InputStream data;
	
	// コンストラクターで変数srcにxml文字列を受け取る
	public ApiData(InputStream in) {
		data = in;
	}

//--------------------------------------------------------------------------------------------	
//	切り出すパラメータのタグを格納する変数

//	ID
	private String idTag;
//	店名
	private String nameTag;
//	緯度
	private String latTag;
//	経度
	private String lngTag;
//	住所
	private String adrsTag;
//	電話番号
	private String telTag;
//	概要
	private String smryTag;

	//IDとタグ名の２次元キーによる配列を実現するため、hashmap内にhashmapを格納する
	private HashMap<String,String> param;
	private HashMap<String,HashMap<String,String>> rootParam;
	
//--------------------------------------------------------------------------------------------	
	
	public String parse(){

		Log.d("XmlPullParserSample", "parse start");

		idTag = "id";
		nameTag = "name";
		latTag = "latitude";
		lngTag = "longitude";
		adrsTag = "address";
		telTag = "tel";
		smryTag = "pr_short";

		//------------------------------------------------------------------------------------
        //パラメータを切り出す
		
		//XmlPullParserのインスタンスを得る
		final XmlPullParser xmlPullParser = Xml.newPullParser();

		//2次元配列。全てのデータを格納するhashmap
		rootParam = new HashMap<String,HashMap<String,String>>();
		
		try {
	    	//xmlデータをxmlPullParserに引き渡す
	    	xmlPullParser.setInput(data,null);
		}catch (Exception e) {
				 Log.d("XmlPullParserSample", "setInputError");
			}

		//xmlタグのステータス（END_DOCUMENT等）をeventTypeに格納する
			int eventType = 0;
        try {
			eventType = xmlPullParser.getEventType();
		} catch (XmlPullParserException e) {
			 Log.d("XmlPullParserSample", "setInputError");
		}

        while (eventType != XmlPullParser.END_DOCUMENT) {

        	try {
        		
        	if(eventType == XmlPullParser.START_TAG) {
	            if(xmlPullParser.getName().equals(idTag)) {
	            	//同じ名前のkeyはvalueが上書きされるため、keyにはidだけでなくidの値も追加している
	        		//1次元配列。1店舗のデータにつき一つのhashmapインスタンスを作り、座標等のデータを格納する
	            	rootParam.put((new String(xmlPullParser.nextText())) + (new String(xmlPullParser.getName())),(param = new HashMap<String,String>()));
	            }else if (xmlPullParser.getName().equals(nameTag)) {
	            	param.put(new String(xmlPullParser.getName()),new String(xmlPullParser.nextText()));
	            } else if (xmlPullParser.getName().equals(latTag)) {
	            	param.put(new String(xmlPullParser.getName()),new String(xmlPullParser.nextText()));
	            } else if (xmlPullParser.getName().equals(lngTag)) {
	            	param.put(new String(xmlPullParser.getName()),new String(xmlPullParser.nextText()));
	            }
        	}
        }catch (Exception e) {
        	Log.d("xmlPullParser", "WhileError");
        }
            try {
				eventType = xmlPullParser.next();
			} catch (Exception e) {
				Log.d("xmlPullParser", "NextError");
			} 
        }
		//------------------------------------------------------------------------------------
        //切り出したパラメータをString型の戻り値に詰め込む

        //選択した座標の内側であるかどうかも判定する

        int cnt = 0;
        String result = null;
        
		for (String rootKey : rootParam.keySet()) {
			cnt++;
			Log.d("xmlPullParserResult", "test2="+rootKey+":cnt="+cnt);
			result += "\n";
			for (String paramKey : rootParam.get(rootKey).keySet()) {
				result += rootParam.get(rootKey).get(paramKey)+ ",";
				Log.d("searchresult",rootParam.get(rootKey).get(paramKey));
			}
		}
		rootParam = null; //メモリを解放する一助としている・・・つもり
		return result;
	}
}
