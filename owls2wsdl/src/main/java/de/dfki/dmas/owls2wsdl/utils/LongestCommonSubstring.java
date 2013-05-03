/***************************************************************************
 *  Longest Common Substring
 *  Copyright (C) 2005 Michael E. Locasto
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU 
 *  General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the:
 *	 Free Software Foundation, Inc.
 *	 59 Temple Place, Suite 330 
 *	 Boston, MA  02111-1307  USA
 *
 * $Id$
 **************************************************************************/

package de.dfki.dmas.owls2wsdl.utils;

import java.util.ArrayList;
import java.util.Collections;

/**
 * The <code>LongestCommonSubstring</code> class houses a single 
 * method for finding the longest common substring of two string inputs.
 * <p>
 * Extensions to this class for finding common substrings for an array,
 * list, or collection of strings are trivial.
 * <p>
 * This implementation is inspired by Jon Bently's Programming Pearls.
 * See:
 *  <br/> <code>http://www.cs.bell-labs.com/cm/cs/pearls/sec152.html</code>
 * for more detail.
 * <p>
 * NO EXPLICIT DUPLICATE DETECTION PERFORMED! In other words, if you have
 * two equally long substrings that occur, we break the tie by returning
 * the lexicographically smaller one.
 * For example, this class handles duplicates in the following way:
 <pre>
[michael@fae ~]$ lcs fccfaa rccyaa
aa
[michael@fae ~]$ lcs fccfaa raaycc
aa
[michael@fae ~]$ lcs faafcc raaycc
aa
[michael@fae ~]$ lcs faafcc rccyaa
aa
[michael@fae ~]$ lcs fccfaa rccyaa
aa
[michael@fae ~]$ lcs fxxfoo xxoo
oo
[michael@fae ~]$ lcs fccfAA rccyAA
AA
[michael@fae ~]$ lcs fccfCC rccyCC
CC
 </pre>
 * The "smaller" substring is returned; thus, 'o' is favored over 'x' and
 * 'a' is favored over 'c'.
 * <p>
 * If you want to gather the whole collection of equal-length longest
 * common substrings, then you need to alter the method below to return
 * a Vector, Array, or ArrayList of such strings.
 * 
 * @author Michael Locasto -- mlocasto@acm.org
 */
public class LongestCommonSubstring
{

   /**
    * Construct the longest common substring between two strings if such
    * a substring exists. Note that this is different from the longest
    * common subsequence in that it assumes you want the longest 
    * continuous sequence. The cost of this routine can be made less by
    * keeping a master copy of data around that you want to check input
    * against. That is, imagine that you keep the sorted suffix arrays
    * around for some collection of data items. Then finding the LCS
    * against that set is just a matter of computing the suffix matrix
    * for the input (e.g., line) and comparing against the pre-computed
    * suffix arrays for each data item.
    * <p>
    * In any event, this routine always computes and sorts the suffix 
    * arrays for both input string parameters.
    *
    * @param data the first string instance
    * @param line the second string instance
    * @return the longest common substring, or the empty string if
    *         at least one of the arguments are <code>null</code>, empty,
    *         or there is no match.
    */
   public String lcs(String data, String line)
   {
      /* BEFORE WE ALLOCATE ANY DATA STORAGE, VALIDATE ARGS */
      if(null==data || "".equals(data))
         return "";
      if(null==line || "".equals(line))
         return "";      
      if(data.equals(line))
         return data;

      /* ALLOCATE VARIABLES WE'LL NEED FOR THE ROUTINE */
      StringBuffer bestMatch = new StringBuffer(0);
      StringBuffer currentMatch = new StringBuffer(1024);
      ArrayList dataSuffixList = new ArrayList();
      ArrayList lineSuffixList = new ArrayList();
      String shorter = null;
      String longer = null; 

      if(data.length()<line.length())
      {
         shorter = data;
         longer = line;
      }else{
         shorter = line;
         longer = data;
      }

      /* Using some builtin String methods, take a couple of shortcuts */ 
      if(longer.startsWith(shorter))
      {
         return shorter;
      }else if(longer.endsWith(shorter)){
         return shorter;
      }

      /* FIRST, COMPUTE SUFFIX ARRAYS */
      for(int i=0;i<data.length();i++)
      {
         dataSuffixList.add(data.substring(i,data.length()));
      }
      for(int i=0;i<line.length();i++)
      {
         lineSuffixList.add(line.substring(i,line.length()));
      }

      /* LEXOGRAPHICALLY SORT SUFFIX ARRAYS (not strictly necessary) */    
      Collections.sort(dataSuffixList);
      Collections.sort(lineSuffixList);
      //System.out.println(dataSuffixList);
      //System.out.println(lineSuffixList);

      /* NOW COMPARE ARRAYS MEMBER BY MEMBER */
      String d = null;
      String l = null;
      String shorterTemp = null;
      String longerTemp = null;
      int stopLength = 0;
      int k = 0;
      boolean match = false;

      bestMatch = new StringBuffer(currentMatch.toString());
      for(int i=0;i<dataSuffixList.size();i++)
      {
         d = dataSuffixList.get(i).toString();
         for(int j=0;j<lineSuffixList.size();j++)
         {
            l = lineSuffixList.get(j).toString();
            //System.out.println(d);
            //System.out.println(l);
            if(d.length()<l.length())
            {
               shorterTemp = d;
               longerTemp = l;
            }else{
               shorterTemp = l;
               longerTemp = d;
            }

            //potentially expensive, but safe
            currentMatch.delete(0,currentMatch.length());
            k=0;
            stopLength = shorterTemp.length();
            /** You can add the assert back in if you compile and run
             *  the program with the appropriate flags to enable asserts
             *  (jdk >=1.4)
             */
            //assert(k<stopLength);

            match = (l.charAt(k)==d.charAt(k));
            while(k<stopLength && match)
            {               
               if(l.charAt(k)==d.charAt(k))
               {
                  //System.out.println("matched");
                  currentMatch.append(shorterTemp.charAt(k));
                  k++;
               }else{
                  match = false;
               }
            }
            //System.out.println("current match = "+currentMatch.toString());
            //System.out.println("best    match = "+bestMatch.toString());
            //got a longer match, so erase bestMatch and replace it.
            if(currentMatch.length()>bestMatch.length())
            {
               //potentially expensive, but safe
               bestMatch.delete(0,bestMatch.length());
               /* replace bestMatch with our current match, which is longer */
               bestMatch = new StringBuffer(currentMatch.toString());
            }
         }
      }
      return bestMatch.toString();
   }

   /**
    * Return all equal-length longest common substrings.
    *
    * @param data
    * @param line
    */
   public ArrayList lcss(String data, String line)
   {
      return null;
   }

   /**
    * Print out usage information.
    */
   private static void doUsage()
   {
      System.err.println("\tjava LongestCommonSubstring <string1> <string2>");
   }

   /**
    * Find the longest common substring between two strings.
    * Invoke by:
    * <pre>
    java LongestCommonSubstring [string1] [string2]
    * </pre>
    */
   public static void main(String [] args)
   {
      LongestCommonSubstring application = null;
      String common = null;

      if(args.length==2)
      {
         application = new LongestCommonSubstring();
         common = application.lcs(args[0], args[1]);
         System.out.println(common);
      }else{
         doUsage();
         
String a = "/htw_kim/thesis/OWLS-MX/owls-tc2/services/1.1/d/_skilledoccupation_BMWservice.owls";
String b = "/htw_kim/thesis/OWLS-MX/owls-tc2/services/1.0/a/_weatherfront_BombayIndiaservice.owls";
application = new LongestCommonSubstring();
common = application.lcs(a, b);
System.out.println(common);
         
         
         System.exit(-1);
      }
      
   }
}
