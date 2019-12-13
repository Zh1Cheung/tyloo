package io.tyloo.tcctransaction.unit.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * Created by changmingxie on 12/2/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/tyloo-unit-test.xml","classpath:/tyloo.xml"})
public abstract class AbstractTestCase {

}