package csvfile;

public class Proportion {
    private static double p=0.0;
    private static int n=0;

    public static void setP(int fv, int iv, int ov){
        if(iv<fv && ov<fv && iv!=0){
            System.out.println("fv:"+fv+"\tiv:"+iv+"\tov:"+ov);
        n++;
        p=((n-1)*p+(fv-iv)/(fv-ov))/n;
        System.out.println(n+"*"+p);}
    }

    public static double getP(){
        return p;
    }
}
