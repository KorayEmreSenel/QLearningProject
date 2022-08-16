import java.util.Random;
import java.util.LinkedList;
import java.util.Queue;

public class QLearningProject {
    static Queue<Integer> stepQue = new LinkedList<>();
    static double alpha = 0.1;
    static double gamma = 0.9;
    static double epsilon = 0.1;
    static int unloadState = 9;
    static int loadState = 99;
    static int rowSize  = 10;
    static double reward = -0.1;
    static boolean isLoaded = false;
    static boolean isDone = false;
    static int columnSize = 10;
    static int Size = rowSize*columnSize;
    static int[] crossStates ={35,42,53,64,75,60,71,82,93};
    static int[] downBlocked={90,91,92,93,94,95,96,97,98,99,15,16,17,76,77};
    static int[] upBlocked={0,1,2,3,4,5,6,7,8,9,46,47,86,87};
    static int[] rightBlocked={9,19,29,39,49,59,69,79,89,99,24};
    static int[] leftBlocked={0,10,20,30,40,50,60,70,80,90,28,38};
    static int[] roughRoad={5,6,7,15,16,17};
    static int[] correctRoad={95,94,84,85,86,87,96,97};
    static Queue<Integer> que = new LinkedList<>();
    static int lastAct;
    static double[][] Q = new double[Size][5];
    static double[][] QStart = new double[Size][5];
    static double[][] QLoaded = new double[Size][5];
    static double[][] QDone = new double[Size][5];
    static Random random = new Random();
    public static void main(String[] args) {
        int j = 0;
        int counter ;
        int iteration = 100000000;
        int startState = 0;
        for (int i = 0; i < iteration; i++) {
            counter = episodes(startState,false);
            if (i > 1000) {
                stepQue.add(counter);
                j++;
                if (j == 20) {
                    if (isLearned(j, counter)) {
                        episodes(startState,true);
                        System.out.print("End");
                        break;
                    }
                    stepQue.remove();
                    j--;
                }
            }
            if (epsilon > 0.001) {
                epsilon = epsilon - 0.00005;
            }
            System.out.println("train " + i + " solved at " + counter + " steps");
        }
    }
    static int actQ(int currentState) {
        double max = -9999999;
        int act = -1;
        int loop = 4;
        if(currentState == loadState || currentState == unloadState){
            loop = 5;
        }
        for (int i = 0; i < loop ; i++) {
            if (max <= Q[currentState][i]) {
                if(max == Q[currentState][i]){
                    act = random.nextInt(2) == 1 ? i : act ;
                    continue;
                }
                max = Q[currentState][i];
                act = i;
            }
        }
        return act;
    }
    public static int takeAction(int act, int currentState) {
        switch (act){
            case 0:
                if(find(leftBlocked,currentState)){
                    return currentState;
                }
                if(find(crossStates,currentState)){
                    if(lastAct == 1){
                        return currentState-1;
                    }
                    else if (lastAct == 2) {
                        if (1 == random.nextInt(2))
                            return currentState ;
                    }
                    else return currentState;
                }
                return currentState-1;
            case 1 :
                if(find(rightBlocked,currentState)){
                    return currentState;
                }
                if(find(crossStates,currentState)){
                    if(lastAct == 0){
                        return currentState+1;
                    }
                    else if (lastAct == 3){
                        if (1 == random.nextInt(2))
                            return currentState;
                    }
                    else return currentState;
                }
                return currentState+1;
            case 2 :
                if(find(upBlocked,currentState)){
                    return currentState;
                }
                if(find(crossStates,currentState)){
                    if(lastAct == 3){
                        return currentState-rowSize;
                    }
                    else if (lastAct == 0){
                        if (1 == random.nextInt(2))
                            return currentState;
                    }
                    else return currentState;
                }
                return currentState-rowSize;
            case 3 :
                if(find(downBlocked,currentState)){
                    return currentState;
                }
                if(find(crossStates,currentState)){
                    if(lastAct == 2){
                        return currentState+rowSize;
                    }
                    else if (lastAct == 1){
                        if (1 == random.nextInt(2))
                            return currentState ;
                    }
                    else return currentState;
                }
                return currentState+rowSize;
            case 4 :
                if (!isLoaded) {
                    if (currentState == loadState) {
                        if(isCorrectRoad()){
                            isLoaded = true;
                            reward = 0;
                        }
                    }
                }
                else {
                    if (currentState == unloadState && !isDone) {
                        isDone = true;
                        reward = 0;
                    }
                }
                return currentState;
        }
        return currentState;
    }
    private static boolean isCorrectRoad() {
        int x = 0;
        for (Integer integer : que) {
            if(find(correctRoad,integer)) x++;
            if(x>2){
                return true;
            }
        }
        return false;
    }
    static void setQ(double value, int s, int a) {
        Q[s][a] = value;
    }
    static double maxQ( int actState) {
        double max = -9999999;
        int loop = 4;
        if(actState == loadState || actState == unloadState){
            loop = 5;
        }
        for (int i = 0; i < loop; i++) {
            if (max < Q[actState][i]) {
                max = Q[actState][i];
            }
        }
        return max;
    }
    static boolean isLearned(int i, int counter) {
        int[] arr ;
        arr = stepQue.stream().mapToInt(Integer::intValue).toArray();
        double sumOfSample = 0;
        double variance;
        double sampleMean;
        for (int j = 0; j < i; j++)
            sumOfSample += arr[j];
        sampleMean = sumOfSample / i;
        double sumVari = 0;
        for (int j = 0; j < i; j++) {
            sumVari += Math.pow(((double) arr[j] - sampleMean), 2);
        }
        variance = sumVari / (double) (i - 1);
        if (variance < 0.1) {
            System.out.println("solved at " + counter + " steps");
            return true;
        } else return false;
    }
    static int episodes( int startState, boolean seeRoad) {
        int counter = 0;
        int agentState = startState; //Initialize s
        boolean flag = true;
        isDone = false;
        isLoaded = false;
        boolean isLoadedSwap = true;
        boolean isDoneSwap = true;
        while (flag) { //Repeat for each state of episode
            counter++;
            int nextState;
            int act ;
            act = findAct(agentState); //Choose a from s using policy derived from epsilon greedy.
            nextState = takeAction(act,agentState);//Take action and observer reward r and next state s'
            if(nextState != agentState){
                lastAct = act;
                if(que.size() > 19){
                    que.remove();
                }
                que.add(agentState);
            }
            if (seeRoad){
                System.out.print(agentState+"->");
            }
            if(find(roughRoad,agentState)){
                reward = -10;
            }
            if(isDone && nextState == startState){
                reward = 1000;
                flag = false;
            }
            double q = Q[agentState][act];//s states Q value
            double max = maxQ(nextState);//Max actions Q value for s'
            double value = q + alpha * (reward + gamma * max - q);//New value of Q(s,a)
            setQ(value, agentState, act);//Update function
            agentState = nextState;
            if(isDoneSwap && isLoaded){
                arrayCopy(Q,QStart);
                arrayCopy(QLoaded,Q);
                if(seeRoad)
                    System.out.println();
                isDoneSwap = false;
            }
            if(isLoadedSwap && isDone){
                arrayCopy(Q,QLoaded);
                arrayCopy(QDone,Q);
                isLoadedSwap = false;
            }
            if(!flag){
                arrayCopy(Q,QDone);
                arrayCopy(QStart,Q);
            }
            reward = -0.1;
        }
        return counter;
    }
    static int findAct(int currentState){
        double rand = random.nextDouble();
        int act ;
        if (epsilon > rand) {
            if(currentState == loadState || currentState == unloadState){
                act = random.nextInt(5);
            }
            else act = random.nextInt(4);
        } else{
            act = actQ(currentState);
        }
        return act;
    }
    static boolean find(int[] arr, int x){
        for (int i = 0; i < arr.length; i++) {
            if (arr[i] == x)
                return true;
        }
        return false;
    }
    static void arrayCopy(double[][]srcArr,double[][]destArr){
        for (int i = 0; i < Size; i++) {
            for (int j = 0; j < 5; j++) {
                destArr[i][j] = srcArr[i][j];
            }
        }
    }
}