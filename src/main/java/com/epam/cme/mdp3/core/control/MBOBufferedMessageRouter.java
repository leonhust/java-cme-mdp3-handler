/*
 * Copyright 2004-2016 EPAM Systems
 * This file is part of Java Market Data Handler for CME Market Data (MDP 3.0).
 * Java Market Data Handler for CME Market Data (MDP 3.0) is free software: you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 * Java Market Data Handler for CME Market Data (MDP 3.0) is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License along with Java Market Data Handler for CME Market Data (MDP 3.0).
 * If not, see <http://www.gnu.org/licenses/>.
 */

package com.epam.cme.mdp3.core.control;

import com.epam.cme.mdp3.MdpGroupEntry;
import com.epam.cme.mdp3.MdpMessage;
import com.epam.cme.mdp3.sbe.schema.MdpMessageTypes;

public class MBOBufferedMessageRouter extends MBOChannelControllerRouter {
    private MBOSnapshotCycleHandler cycleHandler;

    public MBOBufferedMessageRouter(InstrumentManager instrumentManager, MdpMessageTypes mdpMessageTypes, MBOSnapshotCycleHandler cycleHandler) {
        super(instrumentManager, mdpMessageTypes);
        this.cycleHandler = cycleHandler;
    }

    protected void routeEntry(int securityId, MdpMessage mdpMessage, MdpGroupEntry orderIDEntry, MdpGroupEntry mdEntry, long msgSeqNum){
        long snapshotSequence = cycleHandler.getSnapshotSequence(securityId);
        if(snapshotSequence < msgSeqNum) {
            super.routeEntry(securityId, mdpMessage, orderIDEntry, mdEntry, msgSeqNum);
        }
    }
}
