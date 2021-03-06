/*
 * $Id$
 *
 * Copyright 2013 by Swiss Federal Administration
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of the Swiss Federal Administration. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with the Swiss Federal Administration.
 */
package ch.admin.suis.msghandler.common;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author kb
 * @author $Author$
 * @version $Revision$
 * @since 03.01.2013
 */
public class LocalRecipient {

  private final String recipientId;
  
  private Set<Integer> msgTypes = new HashSet<Integer>();

  public LocalRecipient(String recipientId, String msgTypes) {
    this.recipientId = recipientId;
    List<MessageType> messageTypes = MessageType.from(msgTypes);
    for(MessageType msgType : messageTypes){
      this.msgTypes.add(msgType.getType());
    }
  }

  public String getRecipientId() {
    return recipientId;
  }

  public Set<Integer> getMsgTypes() {
    return msgTypes;
  }
  
  public boolean containsMsgType(int msgType){
    return msgTypes.contains(new Integer(msgType));
  }
  
  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder(recipientId + ": [");
    for(Integer i : msgTypes){
      sb.append(Integer.valueOf(i)).append(" ");
    }
    return sb.toString().trim() + "]";
  }
}