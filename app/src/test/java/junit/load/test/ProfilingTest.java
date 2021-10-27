package junit.load.test;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith({ProfilingExtension.class})
class ProfilingTest {

    @Test
    @ProfileTest
    public void multiplyWorks() {
        long product = 1;
        for(int i=1;i<=10;i++){
            product *= i;
        }
        assertTrue(product > 100);
        System.out.println("Product is "+product);
    }

    @Test
    @ProfileTest
    public void sumWorks() {
        long sum = 1;
        for(int i=1;i<=100;i++){
            sum += i;
        }
        assertTrue(sum > 100);
        System.out.println("Sum is "+sum);
    }
}
