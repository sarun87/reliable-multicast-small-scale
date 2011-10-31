/** ********************************************************************
 * File:           InternetChecksum.java 
 * Description:    Method used to calculate the internet checksum.
 * Authors:        Arun, Shyam, Rahul, Venkatesh 
 * Created:        Sun Oct 23 01:17:00 EST 2011
 *
 **********************************************************************/
package p2mp;

/**
 * Class that has the getCheckSum method used to obtain checksum when bytes are
 * given as input.
 */
public class InternetChecksum {

	/**
	 * Method takes in inputData and returns the checksum as long
	 * 
	 * @param inputData
	 * @return checksum
	 */
	public static long getCheckSum(byte[] inputData) {
		long FF00 = 0xff00;
		long FF = 0xff;
		int length = inputData.length;
		int i = 0;

		long sum = 0;
		long data;

		// Handle all pairs
		while (length > 1) {
			data = (((inputData[i] << 8) & FF00) | ((inputData[i + 1]) & FF));
			sum += data;
			// 1's complement carry bit correction in 16-bits (detecting sign
			// extension)
			if ((sum & 0xFFFF0000) > 0) {
				sum = sum & 0xFFFF;
				sum += 1;
			}

			i += 2;
			length -= 2;
		}

		// Handle remaining byte in odd length inputDatafers
		if (length > 0) {
			sum += (inputData[i] << 8 & 0xFF00);
			// 1's complement carry bit correction in 16-bits (detecting sign
			// extension)
			if ((sum & 0xFFFF0000) > 0) {
				sum = sum & 0xFFFF;
				sum += 1;
			}
		}

		// Final 1's complement value correction to 16-bits
		long inverted = ~sum;
		inverted = inverted & 0xFFFF;

		return inverted;
	}
}
