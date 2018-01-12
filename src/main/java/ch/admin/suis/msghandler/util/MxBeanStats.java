/*
 * $Id: MxBeanStats.java 327 2014-01-27 13:07:13Z blaser $
 * <p>
 * Copyright (C) 2006-2012 by Bundesamt für Justiz, Fachstelle für Rechtsinformatik
 * <p>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package ch.admin.suis.msghandler.util;

import java.lang.management.*;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gather statistics from the JVM using MXBeans.
 * <p>
 * Original Source: gf-monitor
 *
 * @author Rafael Wampfler
 * @author $Author: blaser $
 * @version $Revision: 327 $
 */
public final class MxBeanStats {

	private MxBeanStats() {

	}

	/**
	 * Generates a statistic from the Heap. HeapMemoryUsage
	 *
	 * @return A statistic from the heap
	 */
	public static Map<String, String> getHeap() {
		Map<String, String> values = new LinkedHashMap<>();

		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();

		values.put("commited", format(heap.getCommitted()));
		values.put("init", format(heap.getInit()));
		values.put("max", format(heap.getMax()));
		double percent = round(100.0 * heap.getUsed() / heap.getMax(), 1);
		values.put("used", format(heap.getUsed()) + ", " + percent + "%");

		return values;
	}

	/**
	 * Generates a statistic from the PermGen. NonHeapMemoryUsage
	 *
	 * @return a statistic from the permgen
	 */
	public static Map<String, String> getPermGen() {
		Map<String, String> values = new LinkedHashMap<>();

		MemoryUsage nonHeap = ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage();

		values.put("commited", format(nonHeap.getCommitted()));
		values.put("init", format(nonHeap.getInit()));
		values.put("max", format(nonHeap.getMax()));
		double percent = round(100.0 * nonHeap.getUsed() / nonHeap.getMax(), 1);
		values.put("used", format(nonHeap.getUsed()) + ", " + percent + "%");

		return values;
	}

	/**
	 * Generates a map with the current operating system.
	 *
	 * @return a map with the current OS
	 */
	public static Map<String, String> getOperatingSystem() {
		Map<String, String> values = new LinkedHashMap<>();

		OperatingSystemMXBean system = ManagementFactory.getOperatingSystemMXBean();

		values.put("arch", system.getArch());
		values.put("available processors", Integer.toString(system.getAvailableProcessors()));
		values.put("name", system.getName());
		// since Java 1.6. not implemented in Windows because it is "too expensive"
		values.put("system load average", Double.toString(system.getSystemLoadAverage()));
		values.put("version", system.getVersion());

		return values;
	}

	/**
	 * Generates a map with thread information. Such as thread count, peak thread count, and so on...
	 *
	 * @return a map with thread info.
	 */
	public static Map<String, String> getThread() {
		Map<String, String> values = new LinkedHashMap<>();

		ThreadMXBean thread = ManagementFactory.getThreadMXBean();

		values.put("current thread cpu time", Long.toString(thread.getCurrentThreadCpuTime()));
		values.put("current thread user time", Long.toString(thread.getCurrentThreadUserTime()));
		values.put("daemon thread count", Long.toString(thread.getDaemonThreadCount()));
		values.put("peak thread count", Long.toString(thread.getPeakThreadCount()));
		values.put("thread count", Long.toString(thread.getThreadCount()));
		values.put("total started thread count", Long.toString(thread.getTotalStartedThreadCount()));

		return values;
	}

	/**
	 * Generates a map with runtime information. Such as class path, library path, start time and so on...
	 *
	 * @return a map with runtime infos.
	 */
	public static Map<String, String> getRuntime() {
		Map<String, String> values = new LinkedHashMap<>();

		RuntimeMXBean runtime = ManagementFactory.getRuntimeMXBean();

		values.put("boot class path ", runtime.getBootClassPath());
		values.put("class path", runtime.getClassPath());
		values.put("library path", runtime.getLibraryPath());
		values.put("management spec version", runtime.getManagementSpecVersion());
		values.put("name", runtime.getName());
		values.put("spec name", runtime.getSpecName());
		values.put("spec vendor", runtime.getSpecVendor());
		values.put("spec version", runtime.getSpecVersion());
		Date date = new Date(runtime.getStartTime());
		values.put("start time", runtime.getStartTime() + " (" + date + ")");
		double uptime = round(1.0 * runtime.getUptime() / 1000 / 60 / 60 / 24, 1);
		values.put("uptime", runtime.getUptime() + " (" + uptime + "d)");
		values.put("vm name", runtime.getVmName());
		values.put("vm vendor", runtime.getVmVendor());
		values.put("vm version", runtime.getVmVersion());

		return values;
	}

	/**
	 * Generates a map with the loaded classes statistic.
	 *
	 * @return a map with the loaded classes stats.
	 */
	public static Map<String, String> getClassLoading() {
		Map<String, String> values = new LinkedHashMap<>();

		ClassLoadingMXBean loading = ManagementFactory.getClassLoadingMXBean();

		values.put("loaded class count", Long.toString(loading.getLoadedClassCount()));
		values.put("total loaded class count", Long.toString(loading.getTotalLoadedClassCount()));
		values.put("unloaded class count", Long.toString(loading.getUnloadedClassCount()));

		return values;
	}

	private static String format(long number) {
		return round(1.0 * number / 1000 / 1000, 1) + "MB";
	}

	private static double round(double number, int digits) {
		double factor = Math.pow(10, digits);
		return Math.round(factor * number) / factor;
	}
}
