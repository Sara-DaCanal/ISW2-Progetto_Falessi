package csvfile;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class Version{
    private Date releaseDate;
    private String name="";

    public Version(){}
    public Version(String s, Date d){
        this.name=s;
        this.releaseDate=d;
    }

    public void setReleaseDate(String date) throws ParseException {
        this.releaseDate = new SimpleDateFormat("yyyy-MM-dd").parse(date);
    }
    public Date getReleaseDate(){
        return this.releaseDate;
    }

    public void setName(String name) {
        this.name = name;
    }
    public String getName(){
        return this.name;
    }
    @Override
    public boolean equals(Object v){
        if(v==null) return false;
        if(v.getClass()!= this.getClass()) return false;
        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(this.releaseDate);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(((Version)v).releaseDate);
        return (calendar1.get(Calendar.DATE)==calendar2.get(Calendar.DATE) &&
                calendar1.get(Calendar.YEAR)==calendar2.get(Calendar.YEAR) &&
                calendar1.get(Calendar.MONTH)==calendar2.get(Calendar.MONTH));
    }
    @Override
    public int hashCode(){
        return this.hashCode();
    }

}
