import java.util.*;

public class Solution {

    public int maxUniqueSplit(String s) {
        if (s == null || s.length() == 0) {
            return 0;
        }
        return maxUniqueSplit(s, new HashSet<String>());
    }
    
    public int maxUniqueSplit(String s, HashSet<String> seen) {
        if (s.length() == 0) {
            return seen.size();
        }
        
        Integer ans = 1;
        
        StringBuilder sb = new StringBuilder();
        
        for (int idx = 0; idx < s.length(); idx++) {
            sb.append(s.charAt(idx));
            
            if (!seen.contains(sb.toString())) {
                seen.add(sb.toString());
                ans = Math.max(ans, maxUniqueSplit(s.substring(idx + 1), seen));
                seen.remove(sb.toString());
            }
        }
        
        return ans;
    }

    int sub_strs_distinct_chars(String s)
    {
        // Keep track of all characters seen
        int count[] = new int[26];
        int counts = 0;

        int length = s.length();
        int i = 0;
        int j = 0;

        while (j < length)
        {

        }
        return counts;
    }

    public static void main(String[] args)
    {
        Solution s = new Solution();
        System.out.println(s.maxUniqueSplit("abba"));
    }

}
