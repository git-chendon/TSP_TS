package main.basePart;

/**
 * @auther chen.don
 * @date 2019/11/5 10:09
 */
/**
 * 禁忌搜索算法用于解决对称TSP问题
 * 参考csdn博客主wangqiuyun博文
 * 《基于禁忌搜索算法求解TSP问题（JAVA)》2013-04-17 22:38
 */


import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Random;

public class Tabu {

    //城市数量
    private int cityNum;
    //禁忌长度
    private int II;
    //搜索邻居限制
    private int N;
    //最大迭代次数
    private int MAX_GEN;
    //成本单元
    private int[][] distance;
    //初始，最佳，临时，当前编码表，成本总量
    private int[] initGhh;
    private int[] bestGhh;
    private int[] tempGhh;
    private int[] localGhh;

    private int initEvaluate;
    private int bestEvaluate;
    private int tempEvaluate;
    private int localEvaluate;
    //禁忌表,说明：本程序中的禁忌表为目标
    private int[][] jinji;
    //用于产生随机数的Random
    private Random random;
    //当前迭代总数
    private int t;
    //出现最佳结果时的迭代数
    private int bestT;
    //无参构造器
    public Tabu() {

    }
    //含参构造器
    public Tabu(int c,int ii,int m,int n){
        this.cityNum=c;
        this.II=ii;
        this.MAX_GEN=m;
        this.N=n;
    }
    //初始化读取文件,生成distance数组,初始化成员变量
    public void init(String fileName)throws IOException{
        //分别储存x,y
        int[] x;
        int[] y;
        //读取
        String strbuff;

        BufferedReader data=new BufferedReader(
                new InputStreamReader(
                        new FileInputStream(fileName)));
        //初始化数组
        x=new int[cityNum];
        y=new int[cityNum];
        //逐行读取数组赋值
        for(int i=0;i<cityNum;i++){

            strbuff=data.readLine();
            //生成字符串数组
            String[] strs=strbuff.split(" ");
            //填入x,y中

            x[i]=Integer.valueOf(strs[1]);
            y[i]=Integer.valueOf(strs[2]);

        }
        //得到坐标数组后利用循环计算任意两点间距离
        /*
         * 计算要求：任意俩俩之间都要赋值，对于其本身而言距离为0
         * 计算技巧：对称的TSP问题ij与ji值相等计算一次赋值两次就可以
         * 注意点：    1.for循环执行的步骤与过程(解释暂时省略)
         *              2.对于平方开根号出现小数的处理
         *            3.下面的计算方法会漏掉1个解需要补上
         */
        distance=new int[cityNum][cityNum];
        for(int i=0;i<(cityNum-1);i++){
            distance[i][i]=0;
            for(int j=i+1;j<cityNum;j++){
                //先计算距离
                double dij =Math.sqrt(
                        (x[i]-x[j])*(x[i]-x[j])
                                +
                                (y[i]-y[j])*(y[i]-y[j]));
                /*
                 * 赋值分析:这里不采用4舍5入，采取有小数则进位
                 */
                int Dij=(int) Math.round(dij);
                if(Dij<dij){
                    distance[i][j]=Dij+1;
                }else{
                    distance[i][j]=Dij;
                }
                //对称赋值
                distance[j][i]=distance[i][j];
            }

        }
        //将上面漏掉的1个解补上
        distance[cityNum-1][cityNum-1]=0;
        /*
         *到目前为止，已经完成了坐标点的录入以成本单元数组的生成
         *此方法用于程序的初始化，接下来将对剩余成员变量进行初始化
         */

        initGhh=new int[cityNum];
        bestGhh=new int[cityNum];
        tempGhh=new int[cityNum];
        localGhh=new int[cityNum];
        //目标找到最小解，所有初始化均为最大值
        initEvaluate=Integer.MAX_VALUE;
        bestEvaluate=Integer.MAX_VALUE;
        tempEvaluate=Integer.MAX_VALUE;
        localEvaluate=Integer.MAX_VALUE;

        jinji=new int[II][cityNum];
        bestT=0;
        t=0;
        //使得结果不同
        random=new Random(System.currentTimeMillis());

    }
    //用于初始化编码表
    public void initGroup() {
        /*
         * 生成数组initGhh
         * 从0~cityNum互不重复
         * 且随机排列
         */
        int i,j;
        /*
         * 下面的语句用于生成0~cityNum之间的数，且随机性很大
         * 先生成一个数的原因，为了顺利地前后比较
         */
        //65535是union表的最大value
        initGhh[0]=random.nextInt(65535)%cityNum;
        //for不会成为死循环的写法
        for(i=1;i<cityNum;){
            //生成下一个数
            initGhh[i]=random.nextInt(65535)%cityNum;
            //与之前的每一个数进行比较,如果相同则退出循环重来
            for(j=0;j<i;j++){
                if(initGhh[i]==initGhh[j])
                    break;
            }
            if(i==j){
                i++;
            }
        }
        System.out.println("initGhh:");
        for (int k = 0; k < cityNum; k++) {
            System.out.print(initGhh[k] + ",");
        }



    }
    //用于数组数据的拷贝
    public void copyGhh(int[]Gha,int[]Ghb){
        for(int i=0;i<cityNum;i++){
            Ghb[i]=Gha[i];
        }
    }
    //计算总成本量的方法
    public int evaluate(int[] Ghh){
        int totalEvaluate=0;
        for(int i=0;i<(cityNum-1);i++){
            //将01,12,...(city-2)(city-1)的distance值全部加和
            totalEvaluate+=distance[Ghh[i]][Ghh[i+1]];
        }
        //加上由终点返回起点的值
        totalEvaluate+=distance[Ghh[cityNum-1]][Ghh[0]];
        return totalEvaluate;

    }
    //生成一个邻域子集
    public void Linyu(int[] Ghh,int[]tempGhh){
        /*
         *生成邻域子集 的步骤
         *在原编码表中选出两个用于调换的不相同的位置
         *处理tempGhh来进行调换
         */
        copyGhh(Ghh,tempGhh);
        int ran1,ran2 ;
        ran1=random.nextInt(65535)%cityNum;
        ran2=random.nextInt(65535)%cityNum;
        while(ran1==ran2){
            ran2=random.nextInt(65535)%cityNum;
        }
        int temp=tempGhh[ran1];
        tempGhh[ran1]=tempGhh[2];
        tempGhh[2]=temp;
    }
    //禁忌表的处理
    public void handlejinji(int[] tempGhh) {
        /*
         * 同时进行的但是有先后顺序
         * 解禁编码表
         * 添加编码表
         */
        //首先解禁一个最前面的编码表,数据向前推一个单位
        for(int i=1;i<II;i++){
            //对jinji表处理
            for(int j=0;j<cityNum;j++){
                jinji[i-1][j]=jinji[i][j];
            }
        }
        //将一个新的编码表加入禁忌表的最后一列
        for(int j=0;j<cityNum;j++){
            jinji[II-1][j]=tempGhh[j];
        }

    }
    //判断编码表是否在禁忌表中(方法等待优化)
    public boolean isInJinji(int[]Ghh){
        //立一个flag每一次循环前刷新
        int i,j;
        int flag=0;
        for(i=0;i<II;i++){
            flag=0;
            for(j=0;j<cityNum;j++){
                if(jinji[i][j]!=Ghh[j]){
                    flag=1;
                    break;
                }
            }
            //如果循环结束flag没有被改变就说明数据列完全相同
            if(flag==0){
                return true;
            }
        }
        return false;
    }
    //处理业务逻辑
    public void solve() {
        /*进行计算判断求解最优解等一系列事务
         * 第一件事情：得到初始的起点
         * 第二件事情：在允许的最大迭代次数内计算最优解
         */
        //用于记录邻居搜索数量
        int nn = 0;
        initGroup();
        //将初始方法当作最优解
        copyGhh(initGhh,bestGhh);

        bestEvaluate=evaluate(bestGhh);

        //bug等待排查
        //用t来记录当前迭代次数
        while(t<MAX_GEN){
            nn=0;
            localEvaluate=Integer.MAX_VALUE;
            while(nn<N){
                Linyu(initGhh, tempGhh);//使用当前的初始值来拓展
                if(!isInJinji(initGhh)){
                    tempEvaluate=evaluate(tempGhh);
                    if(tempEvaluate<localEvaluate){
                        copyGhh(tempGhh, localGhh);
                        localEvaluate=tempEvaluate;
                    }
                    //仅当不在禁忌表中的时候才算是一个可行的邻域子集
                    nn++;
                }
            }
            if(localEvaluate<bestEvaluate){
                bestT=t;
                copyGhh(localGhh, bestGhh);
                bestEvaluate=localEvaluate;
            }
            copyGhh(localGhh, initGhh);
            //每次循环的localGhh都加禁止
            t++;
        }
        //输出结果：
        System.out.println();
        System.out.println("最佳长度出现代数：");
        System.out.println(bestT);
        System.out.println("最佳长度");
        System.out.println(bestEvaluate);
        System.out.println("最佳路径：");

        for (int i = 0; i < cityNum; i++) {
            System.out.print(bestGhh[i] + ",");
        }
        System.out.println("总迭代数"+t);
    }
    public static void main(String[] args) throws IOException {
        System.out.println("开始读取文件");
//                    Tabu(cityNum,II,MAX_GEN,N)
        Tabu tabu=new Tabu(48,20,1000000,200);
        tabu.init("data.txt");
        tabu.solve();
    }
}