import jdk.nashorn.internal.runtime.regexp.joni.Regex;

import java.util.ArrayList;

/**
 * Created by sf on 2017/9/3.
 */
public class test {
    public static void main(String[] args) {
        ArrayList<String>[] s=new ArrayList[10];
        for(int i=0;i<s.length;i++){
            s[i]=new ArrayList<>();
            s[i].add("Hello");
            s[i].add(" World");
        }
        for(int i=0;i<s.length;i++){
            System.out.println("s"+i+":"+s[i].toString());
        }
    }
}
