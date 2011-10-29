/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package p2mp;

/**
 * 
 * @author Shyam
 */
public class InternetChecksum {

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

		// System.out.println(sum + sum1);

		return inverted;
		// throw new UnsupportedOperationException("Not supported yet.");
	}
}
