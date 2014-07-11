package net.buildbox.pokeri.maps_showmap;


public class ApiData {//yoshida
	
	//fukushima


	private InputStream data;

	// �R���X�g���N�^�[�ŕϐ�src��xml��������󂯎��
	public ApiData(InputStream in) {
		data = in;
	}

//--------------------------------------------------------------------------------------------
//	�؂�o���p�����[�^�̃^�O���i�[����ϐ�

//	ID
	private String idTag;
//	�X��
	private String nameTag;
//	�ܓx
	private String latTag;
//	�o�x
	private String lngTag;
//	�Z��
	private String adrsTag;
//	�d�b�ԍ�
	private String telTag;
//	�T�v
	private String smryTag;

	//ID�ƃ^�O���̂Q�����L�[�ɂ��z����������邽�߁Ahashmap����hashmap���i�[����
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
        //�p�����[�^��؂�o��

		//XmlPullParser�̃C���X�^���X�𓾂�
		final XmlPullParser xmlPullParser = Xml.newPullParser();

		//2�����z��B�S�Ẵf�[�^���i�[����hashmap
		rootParam = new HashMap<String,HashMap<String,String>>();

		try {
	    	//xml�f�[�^��xmlPullParser�Ɉ����n��
	    	xmlPullParser.setInput(data,null);
		}catch (Exception e) {
				 Log.d("XmlPullParserSample", "setInputError");
			}

		//xml�^�O�̃X�e�[�^�X�iEND_DOCUMENT���j��eventType�Ɋi�[����
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
	            	//�������O��key��value���㏑������邽�߁Akey�ɂ�id�����łȂ�id�̒l���ǉ����Ă���
	        		//1�����z��B1�X�܂̃f�[�^�ɂ����hashmap�C���X�^���X�����A���W���̃f�[�^���i�[����
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
        //�؂�o�����p�����[�^��String�^�̖߂�l�ɋl�ߍ���

        //�I���������W�̓����ł��邩�ǂ��������肷��

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
		rootParam = null; //���������������ꏕ�Ƃ��Ă���E�E�E����
		return result;
	}
}
