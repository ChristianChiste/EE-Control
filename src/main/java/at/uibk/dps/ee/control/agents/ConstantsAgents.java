package at.uibk.dps.ee.control.agents;

/**
 * Container for the constants used by the agent classes.
 * 
 * @author Fedor Smirnov
 *
 */
public final class ConstantsAgents {

  private ConstantsAgents() {}

  public static final String ExcMessageEnactment = "Problem while enacting the function task ";

  public static final String ExcMessageExtractionPrefix =
      "Exception when extracting the data from finished task ";
  public static final String ExcMessageExtractionSuffix = " to store it in the data node ";

  public static final String ExcMessageScheduling = "Exception when scheduling the function node ";

  public static final String ExcMessageTransmissionPrefix =
      "Exception during the transmission of data from the node ";
  public static final String ExcMessageTransmissionSuffix = " to the function node ";

}
