import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by sf on 2017/9/3.
 * 培养方案的记录模板
 */
public class TrainingProgram {
    public static void main(String[] args) throws IOException, SQLException {
        File file=new File("result1.txt");
        File resultFIle=new File("result.txt");
        FileWriter output=new FileWriter(resultFIle,true);
        Scanner input=new Scanner(file);
        int num=0;
        String name;//学生姓名
        String ID;//学生ID
//        存储课程类别
        final String[] COURSE_CLASS=new String[]{"经管人文类","公共必修课","基础理论课","专业基础课","专业课","必修环节","选修课"};
//        存储学位课各个分类要求的最少学分，第1项要求的是经管人文类，第6项是必修环节
        final int[] MIN_CREDIE=new int[]{2,6,3,6,4,1,13};
final int TOTAL_CREDIE=32;
//        连接数据库
        Connection conn=connectToMysql();
        Statement statement=conn.createStatement();
        String sql;
        while(input.hasNext()){
//            存储当前学生对象课程获得的学分
            int[] totalCredie=new int[MIN_CREDIE.length];
            name=null;
            ID=null;
            //处理文件头部分
//            以北京邮电大学作为一个学生成绩信息的开始
//            循环直至找到北京邮电大学
            while(!input.nextLine().equals("北京邮电大学"));
            while(!input.nextLine().equals("学号"));
//            获取当前学生ID
            ID=input.nextLine();
            while(!input.nextLine().equals("姓名"));
//            获取当前学生姓名
            name=input.nextLine();
//            输出信息到文件
            output.write("姓名："+name+"\n");
            output.write("学号："+ID+"\n");
            System.out.println(ID);
            while(!input.nextLine().matches("备注"));
//            开始计算学位课程中课程选修情况及是否满足要求
//            课程名

            ArrayList<String>[] course=new ArrayList[COURSE_CLASS.length];
//            此时已经进入到课程表单，开始对课程列表进行处理，
//            到达总学分处表示匹配结束
            String CourseName=input.nextLine();
            while(!CourseName.equals("总学分")) {
                sql = "SELECT class2,credit FROM trainingprogram.sse WHERE Coursename=\"" + CourseName + "\"\n";
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet.next()) {
                    int credit = resultSet.getInt("credit");
                    int class2 = resultSet.getInt("class2");
                    System.out.println(class2);
                    System.out.println(course.length);
                    totalCredie[class2] += credit;
                    course[class2].add(CourseName);
                }
                else{
                    output.write("数据库中没有学生选修的课程课程："+CourseName+"\n");
                }
                input.nextLine();
                input.nextLine();
                CourseName=input.nextLine();

            }
            for(int i=0;i<course.length;i++){
               /* String[] s=new String[course[i].size()];
                course[i].toArray(s);
                s.*/
                Collections.sort(course[i]);
                output.write(COURSE_CLASS[i]+":"+course[i].toString());

            }

         /*   String CourseName=input.nextLine();
            while(!CourseName.equals("总学分")) {
//得分
                String grade_s = input.nextLine();
//                判断是否通过
                boolean isPass=false;
                if(Character.isDigit(grade_s.charAt(0))) {
                    int grade = Integer.parseInt(grade_s);
                    if (grade >= 60) {
                        isPass = true;
                    }
                }
                else
                {
                    if(grade_s.equals("通过"))
                    {
                        isPass=true;
                    }
                    else {
                        output.write("课程"+CourseName+"未通过");
                    }
                }
//                统计学分，来自文件
                int fileCredit=Integer.parseInt(input.nextLine());
//            判断是否及格，若及格，则获取学分及分类，并存入数组
                if (isPass) {
                    sql = "SELECT class2,credit FROM trainingprogram.sse WHERE Coursename=\"" + CourseName + "\"\n";
                    ResultSet resultSet = statement.executeQuery(sql);
                    if (resultSet.next()) {
                        int credit = resultSet.getInt("credit");
                        int class2 = resultSet.getInt("class2");
                        totalCredie[class2] += credit;
                    }
                    else{
                        output.write("数据库中没有学生选修的课程课程："+CourseName+"\n");
                    }
                }
                CourseName=input.nextLine();
            }
//            开始判断学生总学分是否足够
            for(int i=0;i<MIN_CREDIE.length-1;i++){
                if(totalCredie[i]<MIN_CREDIE[i]){
                    output.write(COURSE_CLASS[i]+"学分不足，应增加选修;\n");
                }
            }*/
            output.flush();
         /*   int credieSum=Integer.parseInt(input.nextLine());
            System.out.println("总学分"+(credieSum>TOTAL_CREDIE?"达标":"不足"));
            System.out.println("++++++++++++++++++++++++");*/
        }
        System.out.println(num);
        output.close();
    }
/**
*连接到数据库
*@param
*@return
*@author sf
*/

    public static Connection connectToMysql(){
        return new MysqlOperate().connectToMysql("sf","9536","trainingprogram");
    }
    /**
    *判断数据库中是否有指定的数据
    *@param source 要匹配的字符串
     *              @param tag 要匹配的内容
    *@return 匹配是否成功
    *@author sf
    */

    public static boolean matchWithRelex(String source,String tag){
        return source.matches("*"+tag+"*");
    }
}

/**
*对文件进行预处理
*@param
*@return
*@author sf
*/

class CourseClass{
    public static void main(String[] args) throws IOException {
       File file=new File("test2.txt");
        File resultFIle=new File("result1.txt");
//        在下面要出去只包含这些字符的行
        List<String> l= Arrays.asList("非","学","位","课","程","必修环节","");
        FileWriter output=new FileWriter(resultFIle,true);
        BufferedReader bufferedReader=new BufferedReader(new FileReader(file));
       // Scanner input=new Scanner(file);
        String s;
        while((s=bufferedReader.readLine())!=null){
//            除去无用行及日期
            if(!(l.contains(s)||s.matches("[0-9]{4}.[0-9]{1,2}"))){
                output.write(s+"\n");
            }
        }
    }
}