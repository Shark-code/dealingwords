import java.util.*;
import java.io.*;

public class DealingWords
{
    public static void main(String[] argv) throws FileNotFoundException
    {

        Scanner read = new Scanner(new File("docset1.txt"));
        Scanner stop = new Scanner(new File("stop_words.txt"));

        // clean.txt:
        // lowercase the word between "<TEXT>" and "</TEXT>"
        // and eliminate "not word"
        Formatter clean = new Formatter("data/clean.txt");
        Formatter dictionary = new Formatter("data/docProcessOutput.csv");

        try
        {

            String [] stopping = new String[1000000];
            int counter;
            for(counter = 0; stop.hasNextLine(); ++counter)
                stopping[counter] = stop.nextLine();

            // lowercase
            int countFiles = 0;
            while(read.hasNextLine())
            {
                String word = read.nextLine();
                if(word.equals("<TEXT>"))
                {
                    ++countFiles;
                    clean.format("%s\n", word);
                    word = read.nextLine();
                    while(!word.equals("</TEXT>"))
                    {
                        word = word.toLowerCase();
                        String [] str = word.split(" |\"|\'|&|,|\\.|:|;|-|\\(|\\)|\\[|\\]|\\{|\\}|0|1|2|3|4|5|6|7|8|9|/|_|!|\\?|>|<|=");
                        for(int i = 0; i < str.length; ++i)
                        {
                            boolean ifStop = false;
                            for(int j = 0; j < counter; ++j)
                                if(str[i].equals(stopping[j]))
                                {
                                    ifStop = true;
                                    break;
                                }
                            if(ifStop)
                                continue;
                            clean.format("%s ", str[i]);
                        }
                        if(read.hasNextLine())
                        {
                            clean.format("\n");
                        }
                        word = read.nextLine();
                    }
                }
                clean.format("%s", word);
                if(read.hasNextLine())
                {
                    clean.format("\n");
                }
            }
            clean.close();

            // stemming
            Stemmer stem = new Stemmer();
            String [] a = new String[1];
            a[0] = "data/clean.txt";
            stem.stemming(a);

            // store words
            read = new Scanner(new File("data/AfterStem.txt"));
            ArrayList<String> storeWord = new ArrayList<String>();
            while(read.hasNextLine())
            {
                String word = read.nextLine();
                if(word.equals("<text>"))
                {
                    word = read.nextLine();
                    while(!word.equals("</text>"))
                    {
                        String [] str = word.split(" ");
                        for(int i = 0; i < str.length; ++i)
                            if(str[i].length() != 0)        // get rid of the words that length is 0
                                storeWord.add(str[i]);
                        word = read.nextLine();
                    }
                }
            }

            // sorting
            for(int i = 0; i < storeWord.size(); ++i)
            {
                int minIndex = i;
                for(int j = i + 1; j < storeWord.size(); ++j)
                    if(storeWord.get(j).compareTo(storeWord.get(minIndex)) < 0)
                        minIndex = j;
                String temp = storeWord.get(i);
                storeWord.set(i, storeWord.get(minIndex));
                storeWord.set(minIndex, temp);
            }

            int [] countStore = new int[storeWord.size()];
            for(int i = 0; i < storeWord.size(); ++i)
                if(i > 0 && storeWord.get(i).equals(storeWord.get(i - 1)))
                {
                    ++countStore[i];
                    storeWord.remove(--i);
                }

            for(int i = 0; i < storeWord.size(); ++i)
                dictionary.format("%d %s %d\n", i + 1, storeWord.get(i), countStore[i + 1] + 1);
            dictionary.close();

            // ----------
            System.out.println("Output testing...");
            Scanner test = new Scanner(new File("data/docProcessOutput.csv"));
            boolean hasError = false;
            while(test.hasNextLine())
            {
                String [] word = test.nextLine().split(" ");
                for(int i = 0; i < word[1].length(); ++i)
                {
                    if(word[1].charAt(i) < 'a' || word[1].charAt(i) > 'z')
                    {
                        System.out.println("LINE " + word[0] + ": " + word[1]);
                        hasError = true;
                    }
                }
            }
            if(hasError)
            {
                System.out.println("QQ Go fix your code!!!");
            }
            else
            {
                System.out.println("ALL CORECT XD");
            }
            test.close();
            // ----------

            // task2 start
            read = new Scanner(new File("data/docProcessOutput.csv"));
            String [] dictWords = new String[1000000];
            int [] dictWordsCount = new int[1000000];
            int countLines;
            for(countLines = 0; read.hasNextLine(); ++countLines)
            {
                String [] b = read.nextLine().split(" ");
                dictWords[countLines] = b[1];
                dictWordsCount[countLines] = Integer.valueOf(b[2]);
            }

            Formatter post = new Formatter(new File("data/posting.csv"));
            Formatter tf = new Formatter(new File("data/tf.csv"));

            ArrayList<String> storeDocNums = new ArrayList<String>();
            for(int i = 0; i < countLines; ++i)
            {
                int [] docFreq = new int[100000];
                int [] docWordCount = new int[100000];
                read = new Scanner(new File("data/AfterStem.txt"));
                counter = 0;
                while(read.hasNextLine())
                {
                    String word = read.nextLine();
                    String [] temp = new String[100000];
                    temp = word.split(" ");
                    if(temp[0].equals("<docno>"))
                        storeDocNums.add(temp[1].toUpperCase());
                    if(word.equals("<text>"))
                    {
                        word = read.nextLine();
                        while(!word.equals("</text>"))
                        {
                            String [] str = word.split(" ");
                            for(int j = 0; j < str.length; ++j)
                            {
                                if(str[j].length() == 0)
                                    continue;
                                ++docWordCount[counter];
                                if(str[j].equals(dictWords[i]))
                                    ++docFreq[counter];
                            }
                            word = read.nextLine();
                        }
                        if(word.equals("</text>"))
                            ++counter;
                    }
                }

                post.format("%d %s %d Document Freq:[", i + 1, dictWords[i], dictWordsCount[i]);
                tf.format("%d %s %d Term Freq:[", i + 1, dictWords[i], dictWordsCount[i]);
                for(int j = 0; j < countFiles; ++j)
                {
                    post.format("%d", docFreq[j]);
                    if(j != countFiles - 1)
                        post.format(", ");
                    tf.format("%.3f", docFreq[j] / (docWordCount[j] + 0.0));
                    if(j != countFiles - 1)
                        tf.format(", ");
                }
                post.format("] + DocNo:[");
                tf.format("]\n");

                boolean comma = false;
                for(int j = 0; j < countFiles; ++j)
                {
                    if(comma)
                        if(docFreq[j] != 0)
                            post.format(", ");
                    if(docFreq[j] != 0)
                    {
                        post.format("%s", storeDocNums.get(j));
                        comma = true;
                    }
                }
                post.format("]\n");
                storeDocNums.clear();
            }
            post.close();
            tf.close();

        }
        finally {
            read.close();
            stop.close();
        }
    }
}
