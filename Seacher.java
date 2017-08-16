/*倒排索引的最基本的用法*/

import java.io.File;
import java.io.FileReader;
import java.io.StringReader;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.queryparser.flexible.standard.parser.Token;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Fragmenter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.Scorer;
import org.apache.lucene.search.highlight.SimpleFragmenter;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.highlight.SimpleSpanFragmenter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
public class Searcher {
	public static String [] fp = new String[1000];//用于记录查询到的文件名
	public static String [] keyContent = new String [1000];//用于记录查高亮关键字片段
	public static String info = new String();//用于记录查询到的结果数目
    public static void search(String indexDir, String keyWords,int topNum) throws Exception {
    	//通过关键词，可同时搜索多个fileds
    	String[] fields = { "contents" };
        Directory dir = FSDirectory.open(FileSystems.getDefault().getPath(indexDir));
        IndexReader reader = DirectoryReader.open(dir);
        IndexSearcher is = new IndexSearcher(reader);
        SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer();//此处使用smartchinese分词器,需加入lucene-analyzers-common-6.5.1.jar
        // 创建parser来确定要搜索文件的内容，第一个参数为搜索的域  
        QueryParser queryParser = new MultiFieldQueryParser(fields, analyzer);
        //此处表示输入多个关键词时，空格表示"与"(默认为"或")，即支持多关键词查询
        queryParser.setDefaultOperator(QueryParser.Operator.AND); 
        //QueryParser parser = new QueryParser("contents", analyzer);
        // 创建Query表示搜索域为fields包含KeyWord的文档  
        Query query = queryParser.parse(keyWords);
        //记录开始时间
        long start = System.currentTimeMillis();
        // 根据searcher搜索并且返回TopDocs 
        TopDocs hits = is.search(query, topNum);
       

        QueryScorer scorer = new QueryScorer(query);// 查询得分
        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer);// 得到得分的片段，就是得到一段包含所查询的关键字的摘要
        SimpleHTMLFormatter simpleHTMLFormatter = new SimpleHTMLFormatter(
                "<b><font color='red'>", "</font></b>");// 对查询的数据格式化；无参构造器的默认是将关键字加粗
        Highlighter highlighter = new Highlighter(simpleHTMLFormatter, scorer);// 根据得分和格式化
        //设置高亮相关片段的长度
        //highlighter.setTextFragmenter(new SimpleFragmenter(20));
        highlighter.setTextFragmenter(fragmenter);// 设置成高亮
        int count = 0 ;
        for (ScoreDoc scoreDoc : hits.scoreDocs) {
        	count++;
            Document doc = is.doc(scoreDoc.doc);
            System.out.print(count);
            //记录文件路径
            fp[count] =  doc.get("filepath");
            System.out.println("----"+fp[count]);
            String contents = doc.get("contents");
            if (contents != null) {
                TokenStream tokenStream = analyzer.tokenStream("contents",
                        new StringReader(contents));// TokenStream将查询出来的搞成片段，得到的是整个内容
                //记录高亮片段
                keyContent[count] = highlighter.getBestFragment(tokenStream,contents); 
                System.out.println(keyContent[count]);// 将权重高的摘要显示出来，得到的是关键字内容
            }
        }
        long end = System.currentTimeMillis();
        info = "匹配关键词： " + keyWords + "  ，总共花费 " + (end - start) + "毫秒" + "查询到 "+ hits.totalHits + "条记录 ";
        System.out.println(info);
        reader.close();
    }
}
