package com.onebilliongod.foundation.commons.core.net;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import java.util.TreeMap;

/**
 * Utility class for network-related operations.
 */
public final class NetworkUtils {
    private static final String LOCAL_HOST_NAME = "localhost";
    private static final String LOCAL_HOST_IP = "127.0.0.1";

    private static final String HOST_NAME = hostName();
    private static final String LOCAL_IP = localIp();

    // Returns the local host name, falling back to "localhost" on failure
    private static String hostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (IOException e) {
            return LOCAL_HOST_NAME;
        }
    }

    public static String getHostName() {
        return HOST_NAME;
    }

    public static String getLocalIp() {
        return LOCAL_IP;
    }
    // Retrieves the local IP address
    private static String localIp() {
        try {
            return getHostAddress().getHostAddress();
        } catch (IOException e) {
            return LOCAL_HOST_IP;
        }
    }

    // Gets the first non-loopback IP address
    private static InetAddress getHostAddress() throws IOException {
        Collection<InetAddress> addresses = getNonLoopbackAddresses();
        if (!addresses.isEmpty()) {
            return addresses.iterator().next();
        }
        return InetAddress.getLocalHost(); // Fallback to localhost
    }

    /**
     * Retrieves all non-loopback addresses from the local network interfaces.
     *
     * @return A collection of non-loopback InetAddress instances.
     */
    public static Collection<InetAddress> getNonLoopbackAddresses() {
        Map<Integer, InetAddress> result = new TreeMap<>();
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface network = interfaces.nextElement();
                if (!network.isUp()) {
                    continue; // Skip down interfaces
                }
                collectAddresses(network, result);
            }
        } catch (IOException e) {
            // Log error (consider using a logging framework)

        }

        return result.values();
    }

    // Collects valid addresses from the given network interface
    private static void collectAddresses(NetworkInterface network, Map<Integer, InetAddress> result) {
        Enumeration<InetAddress> addresses = network.getInetAddresses();
        while (addresses.hasMoreElements()) {
            InetAddress address = addresses.nextElement();
            if (isValidAddress(address)) {
                // Store the address, overwriting if it exists
                result.put(network.getIndex(), address);
            }
        }
    }

    /**
     * Validates whether the address is a non-loopback and IPv4 address.
     *
     * @param address The InetAddress to check.
     * @return True if the address is valid; false otherwise.
     */
    private static boolean isValidAddress(InetAddress address) {
        return !address.isLoopbackAddress() && address.isSiteLocalAddress() && (address instanceof Inet4Address);
    }

}
