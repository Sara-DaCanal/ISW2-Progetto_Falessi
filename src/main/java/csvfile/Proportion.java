package csvfile;

public class Proportion {
    private static double p=0.0;
    private static int n=0;

    private Proportion(){
        throw new IllegalStateException("Utility class");
    }

    public static void setP(int fv, int iv, int ov){
        if(iv<fv && ov<fv){
        n++;
        p=((n-1)*p+(double)(fv-iv)/(fv-ov))/n;
        }
    }

    public static double getP(){
        return p;
    }
}
