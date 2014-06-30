package net.buildbox.pokeri.maps_showmap;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.os.AsyncTask;
import android.util.Log;

public class MyAsyncTask extends AsyncTask<String, Integer, String> {

	GoogleMap map = null;
	LatLng[] selectedArea = new LatLng[4];
	
	//コンストラクタでメインスレッドのmapを受け取る
	public MyAsyncTask(GoogleMap tmp, LatLng[] tmp2){
		map = tmp;
		selectedArea = tmp2;
	}
	

	
	@Override
	protected String doInBackground(String... params) {
		
		HttpURLConnection http = null;
        InputStream in = null;

//---------------------------------------------------------------------------------------------
        //クエリに送る位置座標を決めるため、四角形の最小包含円の中心座標を求める（選択エリアの中心を元に、半径xxキロ内の店を検索、という形で結果を絞る）
        //任意の3頂点の外接円を求め、残り1点が内部にあればOK。なければ別の3点の組み合わせで試す
        //２次元平面の想定で計算しているため、四角形が日付変更線をまたいでいる場合は正確な結果を出せない
        
        double tmp[] = new double[8];
        int a,b,c;
        
        for (int i=0 ; i <=3 ; i++){

        	//任意の3頂点を選ぶため、selectedAreaの添字においてあり得る全ての組み合わせ(012,123,230,301)を得る
        	//組み合わせから外れる値をiとすることで(012,123,230,301)のパターンを得る
        	a = 0;
        	if (a == i) { a++; }
		    if (a > 3) { a = 0; } 
		    b = a+1;
		    if (b == i) { b++; }
		    if (b > 3) { b = 0; }
		    c = b+1;
		    if (c == i) { c++; }
		    if (c > 3) { c = 0; }
		    
		    //外心を計算。理屈は未検証・・・
	        tmp[0] = 2 * (selectedArea[b].longitude - selectedArea[a].longitude);
	        tmp[1] = 2 * (selectedArea[b].latitude - selectedArea[a].latitude);
	        tmp[2] = Math.pow(selectedArea[a].longitude,2) - Math.pow(selectedArea[b].longitude,2) + Math.pow(selectedArea[a].latitude,2) - Math.pow(selectedArea[b].latitude,2);
	        tmp[3] = 2 * (selectedArea[c].longitude - selectedArea[a].longitude);
	        tmp[4] = 2 * (selectedArea[c].latitude - selectedArea[a].latitude);
	        tmp[5] = Math.pow(selectedArea[a].longitude,2) - Math.pow(selectedArea[c].longitude,2) + Math.pow(selectedArea[a].latitude,2) - Math.pow(selectedArea[c].latitude,2);
	        //外心のx座標＝longitude
	        tmp[6] = ((tmp[1] * tmp[5]) - (tmp[4] * tmp[2])) / ((tmp[0] * tmp[4]) - (tmp[3] * tmp[1]));
	        //外心のy座標＝latitude
	        tmp[7] = ((tmp[2] * tmp[3]) - (tmp[5] * tmp[0])) / ((tmp[0] * tmp[4]) - (tmp[3] * tmp[1]));
	        
	        //外心の半径
	        double dx = Math.pow(tmp[6] - selectedArea[a].longitude,2);
	        double dy = Math.pow(tmp[7] - selectedArea[a].latitude,2);
	        double distance = Math.sqrt(dx + dy);
	        
	        //外心と残り１点の距離
	        double dx2 = Math.pow(tmp[6] - selectedArea[i].longitude,2);
	        double dy2 = Math.pow(tmp[7] - selectedArea[i].latitude,2);
	        double distance2 = Math.sqrt(dx2 + dy2);
	        
	        //外心と残り１点の距離が半径以内であれば最小包含円が成立している
	        //ダメなら別の１点を外してもう一度計算する
	        if (distance2 <= distance) {
	        	break;
	        }
        }

        Log.d("最終版　外心のx座標",""+tmp[6]);
        Log.d("最終版　外心のy座標",""+tmp[7]);

//---------------------------------------------------------------------------------------------

        String queryUrl = "http://api.gnavi.co.jp/ver1/RestSearchAPI/";
        String apiKey = "2ef4333acaf2e5ea9388911e3c6acdb2";
        String qPage = "50";
        String keyword = null;;
        String qLat = String.valueOf(tmp[7]);
        String qLng = String.valueOf(tmp[6]);
        String range = "5"; //半径3km
        
        //検索キーワードをUTF-8でURLエンコーディングする
        try {
        	keyword = URLEncoder.encode(params[0], "utf-8");
        }catch (UnsupportedEncodingException e){
        	//do nothing
        }
        // ぐるなびAPIへの検索クエリとなるURLを作成
    	queryUrl += "?keyid="+apiKey+"&hit_per_page="+qPage+"&coordinates_mode=2"+"&freeword=" + keyword + "&range=" + range + "&latitude=" + qLat + "&longitude=" + qLng;
    	
//---------------------------------------------------------------------------------------------
		try {
			//URLにHTTP接続
			URL url = new URL(queryUrl);
			http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("GET");
			http.connect();
			// InputStream型変数inにデータをダウンロード
			in = http.getInputStream();
			
			// 検索結果のxmlから必要なパラメータを切り出す
			ApiData apiData = new ApiData(in);
			String src = apiData.parse();
			Log.d("XmlPullParser", "parsed");

			//取得したxmlテキストをonPostExcecuteに引き渡す
			return src;
			
		} catch(Exception e) {
			return e.toString();
	    } finally {
	    	try {
	    		if(http != null)
	    			http.disconnect();
	    		if(in != null) 
	    			in.close();
	    	}catch (Exception e) {}
	    }
	}
	
//	@Override
	protected void onPostExecute(String src){

		//マーカーのオプション用インスタンス
		MarkerOptions options = new MarkerOptions();	
		Marker marker;
		
		//1店舗1行の形で切り出して配列に格納
		String[] strAry = src.split("\n");
		
		// 0行目はnullなので、スキップして1行目から始める
		for (int i = 1 ; i < strAry.length ; i++) {

			//1店舗の各パラメータを切り出して配列に格納
			String[] strAry2 = strAry[i].split(",");
			for (int j = 0 ; j < strAry2.length ; j++) {
			}

			//座標の値をStringからDoubleに型変換
			double lat = Double.parseDouble(strAry2[1]);//lat
			double lng = Double.parseDouble(strAry2[0]);//lng
			
	    	// 表示位置（長押しされた座標）を生成
			// 極端に座標が近い場合は後から生成されたピンが既存のピンを上書きするらしい。「よかたい」で検索するとわかる。
	    	LatLng posMapPoint = new LatLng(lat,lng);
	    	
	    	//選択範囲内かどうか判定する
	    	//店の座標と選択範囲の各辺のベクトルの外積が4辺とも負であれば内部にある。
	    	
			// ピンとタイトル（店名）の設定
			options.position(posMapPoint);
			options.title(strAry2[2]+i);
			// ピンの追加
			marker = map.addMarker(options);
		}
	}
}