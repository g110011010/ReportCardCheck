import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

/**
 * 主要对之前的功能进行整合
 * 分批分段进行编写
 * 设置好全局变量
 * 面向对象编程
 * Created by sf on 2017/9/5.
 */
public class Update {
    final String[] COURSE_CLASS = new String[]{"经管人文类", "公共必修课", "基础理论课", "专业基础课", "专业课", "必修环节", "选修课"};
    //        存储学位课各个分类要求的最少学分，第1项要求的是经管人文类，第6项是必修环节
    final int[] MIN_CREDIE = new int[]{2, 6, 3, 6, 4, 1,19};
    final int TOTAL_CREDIE = 32;
//    程序运行日志
    private String log="errorLog.txt";
//    程序运行日志文件对象
    private File logf;
    //    要处理的文件的路径，绝对路径
    private String sourceFilePath;
//    处理结果输出的文件路径，默认值为runPath/Result/
    private String targetFilePath;
//    处理结果的文件名, 默认值为result.TXT
    private String targetFileName;
//    要处理的文件的文件对象
    private File sourceFile;
//    预处理后的文件对象
    private File pertretMentFile;
//    预处理后的临时文件存放位置
    private String pertretmentFileName="midFile.txt";
//    确定预处理文件是否保存，默认为不保存
    private boolean isSavePertretmentFile;
// 存放最终结果的文件的文件对象
    private File finalFile;
//    连接数据库的对象
    Connection conn;
    Update(String sourceFilePath){
        this(sourceFilePath,"","test11.txt");
    }
    Update(String sourceFilePath,String targetFilePath,String targetFileName){
        this.sourceFilePath=sourceFilePath;
        this.targetFileName=targetFileName;
        this.targetFilePath=targetFilePath;
        sourceFile=new File(sourceFilePath);

        isSavePertretmentFile=false;
//        源文件不存在则报错并退出程序
        if(!sourceFile.exists()){
            System.out.println("Error,file not exist!!!");
            System.exit(1);
        }
        pertretMentFile=new File(targetFilePath+pertretmentFileName);

        finalFile=new File(targetFileName);
        logf=new File(log);
        conn=connectToMysql();
    }
/**
*对输入的文件内容进行预处理
 * 去除大部分无用行
 * 处理后的内容会保存在新的临时文件midFile.txt中
 * 处理后的文本为标准格式
 * 可以在下一步进行课程内容分析处理
*@author sf
*/

    public void filePertretment() throws IOException {
//        在下面要出去只包含这些字符的行
        List<String> l= Arrays.asList("非","学","位","课","程","必修环节","");
        FileWriter output=new FileWriter(pertretMentFile,true);
        BufferedReader bufferedReader=new BufferedReader(new FileReader(sourceFile));
        String s;
        while((s=bufferedReader.readLine())!=null){
//            除去无用行及日期
            if(!(l.contains(s)||s.matches("[0-9]{4}.[0-9]{1,2}"))){
                output.write(s+"\n");
            }
        }
    }
/**
*处理课程信息的主函数所有的工作将在这个函数中完成
*@author sf
*/

public void CourseTretment() throws IOException, SQLException {
    filePertretment();
    FileWriter output = new FileWriter(finalFile);
    FileWriter errorLog=new FileWriter(logf);
    Scanner input = new Scanner(pertretMentFile);
    String name;//学生姓名
    String ID = null;//学生ID


    while (input.hasNext()) {
        try {
            //处理文件头部分
//            以北京邮电大学作为一个学生成绩信息的开始
//            循环直至找到北京邮电大学
            while (!input.nextLine().equals("学号")) ;
//            获取当前学生ID
            ID = input.nextLine();
            while (!input.nextLine().equals("姓名")) ;
//            获取当前学生姓名
            name = input.nextLine();
            System.out.println(ID);
            while (!input.nextLine().matches("备注")) ;

//        存储一个学生的信息
            StudentInfo studentInfo = new StudentInfo();
            studentInfo.setName(name);
            studentInfo.setID(ID);

//            此时已经进入到课程表单，开始对课程列表进行处理，
//            到达总学分处表示匹配结束
            String CourseName = input.nextLine();
            while (!CourseName.equals("总学分")) {
                CourseInfo courseInfo = new CourseInfo();
                courseInfo.setCourseName(CourseName);
                courseInfo.setCourseGrade(Integer.parseInt(input.nextLine()));
                courseInfo.setCretie(Integer.parseInt(input.nextLine()));
                studentInfo.addCourse(courseInfo);
                CourseName = input.nextLine();
            }
            studentInfo.setTotalCredie(String2Int(input.nextLine()));
            input.nextLine();
            studentInfo.setMasterDegreel(Integer.parseInt(input.nextLine()));
            String s = checkStudentGrade(studentInfo);
            String result = writeStudentInfo(studentInfo, true, s, !s.equals(""));
            output.write(result);
            output.flush();
        }
        catch(java.lang.NumberFormatException ex){
            System.out.println("ID:"+ID+"   字符串转化为数字时出错,错误位置："+ex.getMessage());
            errorLog.write("ID:"+ID+"   字符串转化为数字时出错,错误位置："+ex.getMessage()+"\n");
        }
    }

}
public int String2Int(String s) throws java.lang.NumberFormatException{
    java.lang.NumberFormatException e=new java.lang.NumberFormatException(s);
    return Integer.parseInt(s);
    
}
public String checkStudentGrade(StudentInfo studentInfo) throws SQLException {
    String describe="";
    String errorMessage="";
    String message="";
    if(studentInfo.getCourse().size()==0){
        return "";
    }
//    存储学生的学习状况有部分内容不符合要求即为false
    boolean result=true;

    Statement statement=conn.createStatement();
        int[] totalCredie=new int[MIN_CREDIE.length];
        for(int i=0;i<studentInfo.getCourse().size();i++) {
            CourseInfo courseInfo=studentInfo.getCourse().get(i);
            String sql = "SELECT class2,credit FROM trainingprogram.sse WHERE Coursename=\"" + courseInfo.getCourseName() + "\"\n";
            ResultSet resultSet = statement.executeQuery(sql);
            if (resultSet.next()) {
                int credit = resultSet.getInt("credit");
                int class2 = resultSet.getInt("class2");
                totalCredie[class2] += credit;
            } else {
                studentInfo.getCourse().remove(courseInfo);
                i--;
                errorMessage+=writeALine("错误,","数据库中没有学生选修的课程课程：",courseInfo.getCourseName());
            }
        }
        for(int i=0;i<MIN_CREDIE.length-1;i++){
            if(totalCredie[i]<MIN_CREDIE[i]){
                message+=writeALine("课程",COURSE_CLASS[i],"应修",MIN_CREDIE[i]+"","实修",totalCredie[i]+"","应增加选修");
            }
        }
    String sql="INSERT into studentinfo(name, ID, errorMessage, `describe`) VALUES(\""+studentInfo.getName()+"\",\""+studentInfo.getID()+"\",\""+errorMessage+"\",\""+message+"\")\n";
    statement.execute(sql);
    describe=errorMessage+message;
    return describe;
}
/**
*获取一个学生信息的标准化打印输出，对学生的不同类型的课程进行分类后进行输出
*@param studentInfo 包含一个学生的所有信息
 *                   @param isPrintNameAndId 是否打印信息头，即学生的姓名，学号信息
 *                                           @param describe 附加的描述性信息
*@return 返回学生信息的标准输出字符串
*@author sf
*/

    public String writeStudentInfo(StudentInfo studentInfo,boolean isPrintNameAndId,String describe,boolean isprintCourse ) throws SQLException {

        String result = "\n\n\n\n";
        String errorInfo = "";
        if (isPrintNameAndId) {
            result += writeALine("姓名：", studentInfo.getName());
            result += writeALine("学号：", studentInfo.getID());
        }
        if (isprintCourse) {
            Statement statement = conn.createStatement();
            //            开始计算学位课程中课程选修情况及是否满足要求
//            课程名
            ArrayList<CourseInfo>[] course = new ArrayList[COURSE_CLASS.length];
            for (int i = 0; i < course.length; i++) {
                course[i] = new ArrayList<>();
            }
            for (CourseInfo courseInfo : studentInfo.getCourse()) {
                String sql = "SELECT class2,credit FROM trainingprogram.sse WHERE Coursename=\"" + courseInfo.getCourseName() + "\"\n";
                ResultSet resultSet = statement.executeQuery(sql);
                if (resultSet.next()) {
                    int class2 = resultSet.getInt("class2");
                    course[class2].add(courseInfo);
                } else {
                    errorInfo += writeALine("错误,", "数据库中没有学生选修的课程课程：", courseInfo.getCourseName());
                }
            }
            for (int i = 0; i < course.length; i++) {
                result+="\n\n";
                result+=writeALine("-------------------------------------------------------------");
                result += writeALine(COURSE_CLASS[i], ":");
                result+="\n";
                for (CourseInfo courseInfo : course[i]) {
                    result += writeALine(courseInfo.getCourseName(), "   ", "" + courseInfo.getCourseGrade(), "   ", courseInfo.getCretie() + "");
                    result+="\n";
                }
            }
        }
        result+=writeALine("-------------------------------------------------------------");

        result += writeALine("总学分：", studentInfo.getTotalCredie() + "");
            result += writeALine("专业课学分：", studentInfo.getMasterDegreel() + "");
            result+=writeALine("————————————————————————————————————————————————————————————");
            return result + errorInfo + describe;
        }

    public String writeALine(String... str) {
        String result = "";
        for (String s : str) {
            result += s;
        }
        return result + "\n";
    }



    public static Connection connectToMysql(){
        return new MysqlOperate().connectToMysql("sf","9536","trainingprogram");
    }
    public boolean isSavePertretmentFile() {
        return isSavePertretmentFile;
    }

    public void setSavePertretmentFile(boolean savePertretmentFile) {
        isSavePertretmentFile = savePertretmentFile;
    }

    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public String getTargetFilePath() {
        return targetFilePath;
    }

    public String getTargetFileName() {
        return targetFileName;
    }
}
class StudentInfo{
    private String name;
    private String ID;
    //当前学生修习的所有课程

    private ArrayList<CourseInfo> course;
//    总学分
    private int totalCredie;
//    学位课学分
    private int masterDegreel;
    StudentInfo(){
        this.course=new ArrayList<>();
    }
    public int getTotalCredie() {
        return totalCredie;
    }

    public void setTotalCredie(int totalCredie) {
        this.totalCredie = totalCredie;
    }

    public int getMasterDegreel() {
        return masterDegreel;
    }

    public void setMasterDegreel(int masterDegreel) {
        this.masterDegreel = masterDegreel;
    }


    public void addCourse(CourseInfo courseInfo){
        this.course.add(courseInfo);
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getID() {
        return ID;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public ArrayList<CourseInfo> getCourse() {
        return course;
    }

    public void setCourse(ArrayList<CourseInfo> course) {
        this.course = course;
    }
}
class CourseInfo{
    private String courseName;
    private int courseGrade;
    private int cretie;

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public int getCourseGrade() {
        return courseGrade;
    }

    public void setCourseGrade(int courseGrade) {
        this.courseGrade = courseGrade;
    }

    public int getCretie() {
        return cretie;
    }

    public void setCretie(int cretie) {
        this.cretie = cretie;
    }

    public static void main(String[] args) throws IOException, SQLException {
        Update update=new Update("test2.txt");
        update.CourseTretment();
    }
}