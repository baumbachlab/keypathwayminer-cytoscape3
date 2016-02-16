package de.mpg.mpiinf.csb.kpmcytoplugin.util;

import java.util.HashMap;

/**
 * Created by Martin on 03-05-2015.
 */
public class LValueMapper {

    private HashMap<String, Integer> L_OriginalValueMap;

    public LValueMapper(){
        L_OriginalValueMap = new HashMap<String, Integer>();
    }

    public void SetMinValue(String lid, int val){
        L_OriginalValueMap.put(lid + "#min_val", val);
    }

    public void SetStepValue(String lid, int val){
        L_OriginalValueMap.put(lid + "#step_val", val);
    }

    public void SetMaxValue(String lid, int val){
        L_OriginalValueMap.put(lid + "#max_val", val);
    }

    public void SetStdValue(String lid, int val){
        L_OriginalValueMap.put(lid + "#std_val", val);
    }

    public int getMinVal(String lid){
        if(L_OriginalValueMap.containsKey(lid + "#min_val")){
            return L_OriginalValueMap.get(lid + "#min_val");
        }

        return 0;
    }

    public int getStepVal(String lid){
        if(L_OriginalValueMap.containsKey(lid + "#step_val")){
            return L_OriginalValueMap.get(lid + "#step_val");
        }

        return 0;
    }

    public int getMaxVal(String lid){
        if(L_OriginalValueMap.containsKey(lid + "#max_val")){
            return L_OriginalValueMap.get(lid + "#max_val");
        }

        return 0;
    }

    public int getStdVal(String lid){
        if(L_OriginalValueMap.containsKey(lid + "#std_val")){
            return L_OriginalValueMap.get(lid + "#std_val");
        }

        return 0;
    }
}
