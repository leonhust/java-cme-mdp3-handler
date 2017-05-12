package com.epam.cme.mdp3.test;

import com.epam.cme.mdp3.*;
import com.epam.cme.mdp3.core.channel.MdpChannelBuilder;
import com.epam.cme.mdp3.core.channel.MdpFeedException;
import com.epam.cme.mdp3.sbe.message.SbeString;
import com.epam.cme.mdp3.sbe.schema.MdpMessageTypeBuildException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

public class ChannelHelper {
    private static final Logger logger = LoggerFactory.getLogger(ChannelHelper.class);

    private final Object channelInstsHandled = new Object();
    private final SbeString tag55value = SbeString.allocate(20);
    private final SbeString tag1151value = SbeString.allocate(6);

    public Set<InstrumentInfo> resolveInstruments(final String channelId,
                                                         final List<String> symbolGroups, String networkInterface) {
        try {
            final Set<InstrumentInfo> instruments = new HashSet<>();
            final MdpChannel mdpChannel = new MdpChannelBuilder(channelId,
                    Main.class.getResource("/config.xml").toURI(),
                    Main.class.getResource("/templates_FixBinary.xml").toURI())
                    .setNetworkInterface(FeedType.SMBO, Feed.A, networkInterface).setNetworkInterface(FeedType.SMBO, Feed.B, networkInterface)
                    .setNetworkInterface(FeedType.S, Feed.A, networkInterface).setNetworkInterface(FeedType.S, Feed.B, networkInterface)
                    .setNetworkInterface(FeedType.I, Feed.A, networkInterface).setNetworkInterface(FeedType.I, Feed.B, networkInterface)
                    .setNetworkInterface(FeedType.N, Feed.A, networkInterface).setNetworkInterface(FeedType.N, Feed.B, networkInterface)
                    .usingListener(new VoidChannelListener() {
                        @Override
                        public void onFeedStarted(String channelId, FeedType feedType, Feed feed) {
                            logger.info("Channel '{}': {} feed {} is started", channelId, feedType, feed);

                        }

                        @Override
                        public void onFeedStopped(final String channelId, final FeedType feedType, final Feed feed) {
                            if (feedType == FeedType.N) {
                                logger.info("Channel '{}': {} feed {} is stopped", channelId, feedType, feed);
                                synchronized (channelInstsHandled) {
                                    channelInstsHandled.notify();
                                }
                            }
                        }

                        @Override
                        public int onSecurityDefinition(final String channelId, final MdpMessage secDefMessage) {
                            final int securityId = secDefMessage.getInt32(48);
                            secDefMessage.getString(55, tag55value);
                            secDefMessage.getString(1151, tag1151value);

                            final String secGroup = tag1151value.getString();
                            if (symbolGroups.contains(secGroup)) {
                                instruments.add(new InstrumentInfo(securityId, tag55value.getString()));
                            }

                            return MdEventFlags.NOTHING;
                        }
                    })
                    .build();

            mdpChannel.startInstrumentFeedA();
            synchronized (channelInstsHandled) {
                channelInstsHandled.wait(TimeUnit.MINUTES.toMillis(2));
            }
            mdpChannel.close();
            return instruments;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String args[]) {
        try {
            final Set<InstrumentInfo> instruments = new ChannelHelper().resolveInstruments("320", Arrays.asList( "6C", "6E", "6M", "6N"), null);
            System.out.println(instruments);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
