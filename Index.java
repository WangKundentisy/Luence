/*建立索引*/



import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.cn.smart.SmartChineseAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Field.Store;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

class Index {
    // 建立索引
    public void index(String indexStr,String documentFile) {
        IndexWriter indexWriter = null;
        //System.out.println("合并因子"+indexWriter.SOURCE_MERGE);

        try {
            // 1、创建Directory
            //JDK 1.7以后 open只能接收Path
        
            Directory directory = FSDirectory.open(FileSystems.getDefault().getPath(indexStr));
            // 2、创建IndexWriter
            //Analyzer analyzer = new StandardAnalyzer();
            SmartChineseAnalyzer analyzer = new SmartChineseAnalyzer(); //此处使用smartchinese分词器 ,需加入lucene-analyzers-common-6.5.1.jar
            IndexWriterConfig indexWriterConfig = new IndexWriterConfig(analyzer);
            indexWriter = new IndexWriter(directory, indexWriterConfig);
            /*indexWriter.deleteAll();清除以前的index*/
            //要搜索的File路径
            File dFile = new File(documentFile);
            File[] files = dFile.listFiles();
            for (File file : files) {
            	//表示文件内容
            	String contents = "";
                // 3、创建Document对象
                Document document = new Document();
                // 4、为Document添加Field
                // 第三个参数是FieldType 但是定义在TextField中作为静态变量，看API也不好知道怎么写
                String type = file.getName().substring(file.getName().lastIndexOf(".")+1);
                if("txt".equalsIgnoreCase(type)){
                    
                    contents += txt2String(file);
                }
                document.add(new TextField("contents", contents, Store.YES));
               // document.add(new Field("content", new FileReader(file), TextField.TYPE_NOT_STORED));
                document.add(new Field("filename", file.getName(), TextField.TYPE_STORED));
                document.add(new Field("filepath", file.getAbsolutePath(), TextField.TYPE_STORED));
                //Field FieldContent = new TextField("contents", FileReader.readFile(file), Field.Store.YES);
                //document.add(new TextField("contents", new BufferedReader(new InputStreamReader(Files.newInputStream(Paths.get(file.getAbsolutePath())), StandardCharsets.UTF_8))));
                // 5、通过IndexWriter添加文档到索引中
                indexWriter.addDocument(document);
            }
              
            indexWriter.close();  
            System.out.println("索引建立成功！");

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (indexWriter != null) {
                    indexWriter.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    //读文件内容的操作
    public static String txt2String(File file){
        String result = "";
        try{
            BufferedReader br = new BufferedReader(new FileReader(file));//构造一个BufferedReader类来读取文件
            String s = null;
            while((s = br.readLine())!=null){//使用readLine方法，一次读一行
                result = result + "\n" +s;
            }
            br.close();    
        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }
}
