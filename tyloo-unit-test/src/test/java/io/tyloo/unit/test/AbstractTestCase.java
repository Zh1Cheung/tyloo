package io.tyloo.unit.test;

import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "classpath:/tyloo-unit-test.xml","classpath:/tyloo.xml"})
public abstract class AbstractTestCase {

}