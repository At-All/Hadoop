package cn.bjfu.test;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.apache.log4j.BasicConfigurator;

import java.io.IOException;

/**
 * DDL:
 * 0.获取 Configuration 对象
 * 1.判断表是否存在
 * 2.创建表
 * 3.删除表
 * 4.创建命名空间
 *
 * DML:
 * 5.向表中插入数据
 * 6.删除多行数据
 * 7.获取所有数据
 * 8.获取某一行数据
 * 9.获取某一行指定“列族:列”的数据
 *
 */
public class TestAPI {

    //获取 HBaseAdmin 对象,在 HBase 中管理、访问表需要先创建 HBaseAdmin 对象
    public static HBaseAdmin admin=null;
    //获取 Configuration 对象,初始化 HBaseAdmin
    public static Configuration conf;
    static{
        //使用 HBaseConfiguration 的单例方法实例化
        conf = HBaseConfiguration.create();
        conf.set("hbase.zookeeper.quorum", "192.168.64.132");
        conf.set("hbase.zookeeper.property.clientPort", "2181");
        try {
            admin = new HBaseAdmin(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //判断表是否存在
    public static boolean isTableExist(String tableName) throws IOException {
        return admin.tableExists(tableName);
    }

    //创建表
    public static void createTable(String tableName, String... columnFamily) throws IOException {
        //判断表是否存在
        if(isTableExist(tableName)){
            System.out.println("表" + tableName + "已存在");
            //System.exit(0);
        }else{
            //创建表属性对象,表名需要转字节
            HTableDescriptor descriptor = new HTableDescriptor(TableName.valueOf(tableName));
            //创建多个列族
            for(String cf : columnFamily){
                descriptor.addFamily(new HColumnDescriptor(cf));
            }
            //根据对表的配置，创建表
            admin.createTable(descriptor);
            System.out.println("表" + tableName + "创建成功！");
        }
    }

    //删除表
    public static void dropTable(String tableName) throws IOException{
        if(isTableExist(tableName)){
            admin.disableTable(tableName);
            admin.deleteTable(tableName);
            System.out.println("表" + tableName + "删除成功！");
        }else{
            System.out.println("表" + tableName + "不存在！");
        }
    }

    //创建命名空间
    public static void creatNameSpace(String ns) {
        NamespaceDescriptor namespaceDescriptor = NamespaceDescriptor.create(ns).build();
        try {
            admin.createNamespace(namespaceDescriptor);
        } catch (NamespaceExistException e){
            System.out.println(" Namespace " + ns + " already exists");
            return;
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("create Namespace " + ns + " success!");
    }

    //向表中插入数据
    public static void addRowData(String tableName, String rowKey, String columnFamily, String column, String value) throws IOException{
        //创建 HTable 对象
        HTable hTable = new HTable(conf, tableName);
        //向表中插入数据
        Put put = new Put(Bytes.toBytes(rowKey));
        //向 Put 对象中组装数据
        put.addColumn(Bytes.toBytes(columnFamily), Bytes.toBytes(column),Bytes.toBytes(value));
        hTable.put(put);
        hTable.close();
        System.out.println("插入数据成功");
    }

    //删除数据
    public static void deleteRow(String tableName, String row) throws IOException{
        HTable hTable = new HTable(conf, tableName);
        Delete delete = new Delete(Bytes.toBytes(row));
        hTable.delete(delete);
        hTable.close();
        System.out.println("删除数据成功！");
    }

    //扫描数据
    public static void getRows(String tableName) throws IOException{
        HTable hTable = new HTable(conf, tableName);
        //得到用于扫描 region 的对象
        Scan scan = new Scan();
        //使用 HTable 得到 resultcanner 实现类的对象
        ResultScanner resultScanner = hTable.getScanner(scan);
        System.out.println("行 键 " +"列 族 "+" 列 " + "  值 ");
        for(Result result : resultScanner){
            Cell[] cells = result.rawCells();
            for(Cell cell : cells){
                //得到 rowkey
                System.out.print(Bytes.toString(CellUtil.cloneRow(cell))+" ");
                //得到列族
                System.out.print(Bytes.toString(CellUtil.cloneFamily(cell))+" ");
                System.out.print(Bytes.toString(CellUtil.cloneQualifier(cell))+" ");
                System.out.println(Bytes.toString(CellUtil.cloneValue(cell))+" ");
            }
        }
        hTable.close();
    }

    //获取数据
    public static void getRow(String tableName, String rowKey) throws IOException{
        HTable table = new HTable(conf, tableName);
        Get get = new Get(Bytes.toBytes(rowKey));
        //get.setMaxVersions();显示所有版本
        //get.setTimeStamp();显示指定时间戳的版本
        Result result = table.get(get);
        for(Cell cell : result.rawCells()){
            System.out.print(" 行 键 :" +Bytes.toString(result.getRow()));
            System.out.print(" 列 族 " + Bytes.toString(CellUtil.cloneFamily(cell)));
            System.out.print(" 列 :" + Bytes.toString(CellUtil.cloneQualifier(cell)));
            System.out.print(" 值 :" + Bytes.toString(CellUtil.cloneValue(cell)));
            System.out.println(" 时间戳:" + cell.getTimestamp());
        }
        table.close();
    }

    public static void main(String[] args) throws IOException {
        //BasicConfigurator.configure(); //自动快速地使用缺省Log4j环境。

        //判断表是否存在
//        System.out.println(isTableExist("student"));
//        System.out.println(isTableExist("student1"));

        //创建表
//        createTable("stu","info");
//        createTable("stu","info1","info2");

        //删除表
//        dropTable("stu");

        //创建命名空间
//        creatNameSpace("0408");

        //向表中插入数据
//        addRowData("stu","1001","info1","name","wangkang");
//        addRowData("stu","1001","info1","sex","boy");
//        addRowData("stu","1001","info1","age","19");

        //删除数据
//        deleteRow("stu","1001");

        //查询数据
//        getRows("stu");

        //获取数据
        getRow("stu","1001");


        admin.close();
    }
}
