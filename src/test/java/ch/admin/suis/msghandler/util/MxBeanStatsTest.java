package ch.admin.suis.msghandler.util;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class MxBeanStatsTest {

	@Test
	public void testHeapMethod() {
		assertNotNull(MxBeanStats.getHeap());
	}

	@Test
	public void testOperatingSystemInfo() {
		assertNotNull(MxBeanStats.getOperatingSystem());
	}

	@Test
	public void testThreadInfos() {
		assertNotNull(MxBeanStats.getThread());
	}

	@Test
	public void testClassLoading() {
		assertNotNull(MxBeanStats.getClassLoading());
	}

	@Test
	public void testRuntime() {
		assertNotNull(MxBeanStats.getRuntime());
	}

	@Test
	public void testPermGen() {
		assertNotNull(MxBeanStats.getPermGen());
	}

}
