package com.apiherd.api;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.SimpleTimeZone;
import java.util.UUID;

public class DateHelper {

	private final static String TIME_ZONE = "GMT";
	private final static String FORMAT_ISO8601 = "yyyy-MM-dd'T'HH:mm:ss'Z'";
	private final static String FORMAT_RFC2616 = "EEE, dd MMM yyyy HH:mm:ss zzz";
	
    public DateHelper() {
    }

    public static String getUniqueNonce() {
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }

    public static String getISO8601Time(Date date) {
    	Date nowDate = date;
    	if (null == date){
    		nowDate = new Date();
    	}
        SimpleDateFormat df = new SimpleDateFormat(FORMAT_ISO8601);
        df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        
        return df.format(nowDate);
    }

    public static String getRFC2616Date(Date date) {
    	Date nowDate = date;
    	if (null == date){
    		nowDate = new Date();
    	}
        SimpleDateFormat df = new SimpleDateFormat(FORMAT_RFC2616, Locale.ENGLISH);
        df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        return df.format(nowDate);
    }

    public static Date parse(String strDate) throws ParseException {
    	if (null == strDate || "".equals(strDate)){
    		return null;
    	}
        try {
        	return parseISO8601(strDate);
        } catch (ParseException exp) {
        	return parseRFC2616(strDate);
        }
    }
    
    public static Date parseISO8601(String strDate) throws ParseException {
    	if (null == strDate || "".equals(strDate)){
    		return null;
    	}
    	SimpleDateFormat df = new SimpleDateFormat(FORMAT_ISO8601);
    	df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        return df.parse(strDate);  
    }
    
    public static Date parseRFC2616(String strDate) throws ParseException {
    	if (null == strDate || "".equals(strDate) || strDate.length() != FORMAT_RFC2616.length()){
    		return null;
    	}
    	SimpleDateFormat df = new SimpleDateFormat(FORMAT_RFC2616, Locale.ENGLISH);
    	df.setTimeZone(new SimpleTimeZone(0, TIME_ZONE));
        return df.parse(strDate);  
    }
}