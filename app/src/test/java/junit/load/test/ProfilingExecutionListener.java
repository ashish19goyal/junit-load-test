package junit.load.test;
import org.junit.platform.launcher.*;

public class ProfilingExecutionListener implements TestExecutionListener{
    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        System.out.println("Executed test plan = " + testPlan.toString());
    }
}
