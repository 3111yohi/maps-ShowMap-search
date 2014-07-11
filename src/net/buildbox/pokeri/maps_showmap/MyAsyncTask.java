package net.buildbox.pokeri.maps_showmap;

import java.io.InputStream;//yosjida
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
	
	//�R���X�g���N�^�Ń��C���X���b�h��map���󂯎��
	public MyAsyncTask(GoogleMap tmp, LatLng[] tmp2){
		map = tmp;
		selectedArea = tmp2;
	}
	

	
	@Override
	protected String doInBackground(String... params) {
		
		HttpURLConnection http = null;
        InputStream in = null;

//---------------------------------------------------------------------------------------------
        //�N�G���ɑ���ʒu���W�����߂邽�߁A�l�p�`�̍ŏ���܉~�̒��S���W�����߂�i�I���G���A�̒��S�����ɁA���axx�L�����̓X�������A�Ƃ����`�Ō��ʂ��i��j
        //�C�ӂ�3���_�̊O�ډ~�����߁A�c��1�_�������ɂ����OK�B�Ȃ���Εʂ�3�_�̑g�ݍ��킹�Ŏ���
        //�Q�������ʂ̑z��Ōv�Z���Ă��邽�߁A�l�p�`�����t�ύX�����܂����ł���ꍇ�͐��m�Ȍ��ʂ��o���Ȃ�
        
        double tmp[] = new double[8];
        int a,b,c;
        
        for (int i=0 ; i <=3 ; i++){

        	//�C�ӂ�3���_��I�Ԃ��߁AselectedArea�̓Y���ɂ����Ă��蓾��S�Ă̑g�ݍ��킹(012,123,230,301)�𓾂�
        	//�g�ݍ��킹����O���l��i�Ƃ��邱�Ƃ�(012,123,230,301)�̃p�^�[���𓾂�
        	a = 0;
        	if (a == i) { a++; }
		    if (a > 3) { a = 0; } 
		    b = a+1;
		    if (b == i) { b++; }
		    if (b > 3) { b = 0; }
		    c = b+1;
		    if (c == i) { c++; }
		    if (c > 3) { c = 0; }
		    
		    //�O�S���v�Z�B�����͖����؁E�E�E
	        tmp[0] = 2 * (selectedArea[b].longitude - selectedArea[a].longitude);
	        tmp[1] = 2 * (selectedArea[b].latitude - selectedArea[a].latitude);
	        tmp[2] = Math.pow(selectedArea[a].longitude,2) - Math.pow(selectedArea[b].longitude,2) + Math.pow(selectedArea[a].latitude,2) - Math.pow(selectedArea[b].latitude,2);
	        tmp[3] = 2 * (selectedArea[c].longitude - selectedArea[a].longitude);
	        tmp[4] = 2 * (selectedArea[c].latitude - selectedArea[a].latitude);
	        tmp[5] = Math.pow(selectedArea[a].longitude,2) - Math.pow(selectedArea[c].longitude,2) + Math.pow(selectedArea[a].latitude,2) - Math.pow(selectedArea[c].latitude,2);
	        //�O�S��x���W��longitude
	        tmp[6] = ((tmp[1] * tmp[5]) - (tmp[4] * tmp[2])) / ((tmp[0] * tmp[4]) - (tmp[3] * tmp[1]));
	        //�O�S��y���W��latitude
	        tmp[7] = ((tmp[2] * tmp[3]) - (tmp[5] * tmp[0])) / ((tmp[0] * tmp[4]) - (tmp[3] * tmp[1]));
	        
	        //�O�S�̔��a
	        double dx = Math.pow(tmp[6] - selectedArea[a].longitude,2);
	        double dy = Math.pow(tmp[7] - selectedArea[a].latitude,2);
	        double distance = Math.sqrt(dx + dy);
	        
	        //�O�S�Ǝc��P�_�̋���
	        double dx2 = Math.pow(tmp[6] - selectedArea[i].longitude,2);
	        double dy2 = Math.pow(tmp[7] - selectedArea[i].latitude,2);
	        double distance2 = Math.sqrt(dx2 + dy2);
	        
	        //�O�S�Ǝc��P�_�̋��������a�ȓ��ł���΍ŏ���܉~���������Ă���
	        //�_���Ȃ�ʂ̂P�_���O���Ă�����x�v�Z����
	        if (distance2 <= distance) {
	        	break;
	        }
        }

        Log.d("�ŏI�Ł@�O�S��x���W",""+tmp[6]);
        Log.d("�ŏI�Ł@�O�S��y���W",""+tmp[7]);

//---------------------------------------------------------------------------------------------

        String queryUrl = "http://api.gnavi.co.jp/ver1/RestSearchAPI/";
        String apiKey = "2ef4333acaf2e5ea9388911e3c6acdb2";
        String qPage = "50";
        String keyword = null;;
        String qLat = String.valueOf(tmp[7]);
        String qLng = String.valueOf(tmp[6]);
        String range = "5"; //���a3km
        
        //�����L�[���[�h��UTF-8��URL�G���R�[�f�B���O����
        try {
        	keyword = URLEncoder.encode(params[0], "utf-8");
        }catch (UnsupportedEncodingException e){
        	//do nothing
        }
        // ����Ȃ�API�ւ̌����N�G���ƂȂ�URL���쐬
    	queryUrl += "?keyid="+apiKey+"&hit_per_page="+qPage+"&coordinates_mode=2"+"&freeword=" + keyword + "&range=" + range + "&latitude=" + qLat + "&longitude=" + qLng;
    	
//---------------------------------------------------------------------------------------------
		try {
			//URL��HTTP�ڑ�
			URL url = new URL(queryUrl);
			http = (HttpURLConnection)url.openConnection();
			http.setRequestMethod("GET");
			http.connect();
			// InputStream�^�ϐ�in�Ƀf�[�^���_�E�����[�h
			in = http.getInputStream();
			
			// �������ʂ�xml����K�v�ȃp�����[�^��؂�o��
			ApiData apiData = new ApiData(in);
			String src = apiData.parse();
			Log.d("XmlPullParser", "parsed");

			//�擾����xml�e�L�X�g��onPostExcecute�Ɉ����n��
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

		//�}�[�J�[�̃I�v�V�����p�C���X�^���X
		MarkerOptions options = new MarkerOptions();	
		Marker marker;
		
		//1�X��1�s�̌`�Ő؂�o���Ĕz��Ɋi�[
		String[] strAry = src.split("\n");
		
		// 0�s�ڂ�null�Ȃ̂ŁA�X�L�b�v����1�s�ڂ���n�߂�
		for (int i = 1 ; i < strAry.length ; i++) {

			//1�X�܂̊e�p�����[�^��؂�o���Ĕz��Ɋi�[
			String[] strAry2 = strAry[i].split(",");
			for (int j = 0 ; j < strAry2.length ; j++) {
			}

			//���W�̒l��String����Double�Ɍ^�ϊ�
			double lat = Double.parseDouble(strAry2[1]);//lat
			double lng = Double.parseDouble(strAry2[0]);//lng
			
	    	// �\���ʒu�i���������ꂽ���W�j�𐶐�
			// �ɒ[�ɍ��W���߂��ꍇ�͌ォ�琶�����ꂽ�s���������̃s�����㏑������炵���B�u�悩�����v�Ō�������Ƃ킩��B
	    	LatLng posMapPoint = new LatLng(lat,lng);
	    	
	    	//�I��͈͓����ǂ������肷��
	    	//�X�̍��W�ƑI��͈͂̊e�ӂ̃x�N�g���̊O�ς�4�ӂƂ����ł���Γ����ɂ���B
	    	
			// �s���ƃ^�C�g���i�X���j�̐ݒ�
			options.position(posMapPoint);
			options.title(strAry2[2]+i);
			// �s���̒ǉ�
			marker = map.addMarker(options);
		}
	}
}