package com.onebilliongod.foundation.commons.core;

import java.net.InetAddress;
import java.util.Collection;

import com.onebilliongod.foundation.commons.core.net.NetworkUtils;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class NetworkUtilsTest {
    @Test
    public void testGetHostName() {
        String hostName = NetworkUtils.getHostName();
        assertNotNull(hostName);
        assertFalse(hostName.isEmpty());
    }

    @Test
    public void testGetLocalIp() {
        String localIp = NetworkUtils.getLocalIp();
        assertNotNull(localIp);
        assertFalse(localIp.isEmpty());
        assertTrue(localIp.equals("localhost") || isValidIp(localIp));
    }

    @Test
    public void testGetNonLoopbackAddresses() {
        Collection<InetAddress> addresses = NetworkUtils.getNonLoopbackAddresses();
        assertNotNull(addresses);
        assertFalse(addresses.isEmpty()); // May need to adjust based on network conditions
    }

    // Simple validation for IP format
    private boolean isValidIp(String ip) {
        String ipPattern = "^\\d{1,3}(\\.\\d{1,3}){3}$";
        return ip.matches(ipPattern);
    }
}
