/*
 * This file is part of NeverNote 
 * Copyright 2009 Randy Baumgarte
 * 
 * This file may be licensed under the terms of of the
 * GNU General Public License Version 2 (the ``GPL'').
 *
 * Software distributed under the License is distributed
 * on an ``AS IS'' basis, WITHOUT WARRANTY OF ANY KIND, either
 * express or implied. See the GPL for the specific language
 * governing rights and limitations.
 *
 * You should have received a copy of the GPL along with this
 * program. If not, go to http://www.gnu.org/licenses/gpl.html
 * or write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 *
*/
package cx.fbn.nevernote.evernote;

//**********************************************
//**********************************************
//* Utility used to encript or decrypt the 
//* text in a note.
//**********************************************
//**********************************************

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.zip.CRC32;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.RC2ParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import cx.fbn.nevernote.utilities.Base64;

public class EnCrypt {

	// Convert a string of text to a hex string
     public static String asHex (byte buf[]) {
      StringBuffer strbuf = new StringBuffer(buf.length * 2);
      int i;

      for (i = 0; i < buf.length; i++) {
       if ((buf[i] & 0xff) < 0x10)
	    strbuf.append("0");

       strbuf.append(Long.toString(buf[i] & 0xff, 16));
      }

      return strbuf.toString();
     }
	// Encrypte the text and return the base64 string
	public String encrypt(String text, String passphrase, int keylen) {
		RC2ParameterSpec parm = new RC2ParameterSpec(keylen);
	    MessageDigest md;
		try {
			int len = text.length()+4;
			int mod = (len%8);
			if (mod>0) {
				for (; mod !=0; len++) {
					mod = len%8;
				}
				len--;
			}
			len = len-4;
			StringBuffer textBuffer = new StringBuffer(text);
			textBuffer.setLength(len);
			// Get a MD5 for the passphrase
			md = MessageDigest.getInstance("MD5");
		    md.update(passphrase.getBytes());
		    
		    // Setup parms for the cipher
		    SecretKeySpec skeySpec = new SecretKeySpec(md.digest(), "RC2");
			Cipher cipher = Cipher.getInstance("RC2/ECB/NoPadding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, parm);
			String encoded = crcHeader(textBuffer.toString()) +textBuffer;
			byte[] d = cipher.doFinal(encoded.getBytes());
			return Base64.encodeBytes(d);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		}		
		
		return null;
	}
	// Decrypt the base64 text and return the unsecure text
	public String decrypt(String text, String passphrase, int keylen) {
		RC2ParameterSpec parm = new RC2ParameterSpec(keylen);
	    MessageDigest md;
		try {
			// Get a MD5 for the passphrase
			md = MessageDigest.getInstance("MD5");
			StringBuffer p = new StringBuffer(passphrase);
			md.update(p.toString().getBytes());
		    
		    // Setup parms for the cipher
		    SecretKeySpec skeySpec = new SecretKeySpec(md.digest(), "RC2");
			Cipher cipher = Cipher.getInstance("RC2/ECB/NOPADDING");
			cipher.init(Cipher.DECRYPT_MODE, skeySpec, parm);
			
			// Decode the encrypted text and decrypt
			byte[] dString = Base64.decode(text);
			byte[] d = cipher.doFinal(dString);
			
			// We have a result.  Separate it into the 4 byte header and the decrypted text
			StringBuffer buffer = new StringBuffer(new String(d));
			String cryptCRC = buffer.substring(0,4);
			String clearText = buffer.substring(4);
			String realCRC = crcHeader(clearText);
			// We need to get the real CRC of the decrypted text
			if (realCRC.equalsIgnoreCase(cryptCRC)) {
				int endPos = clearText.length();
				for (int i=buffer.length()-1; i>=0; i--) {
					if (buffer.charAt(i) == 0) 
						endPos--;
					else
						i=-1;
				}
				clearText = clearText.substring(0,endPos);
				return clearText;
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}			
		return null;
	}
	// Utility function to return the CRC header of an encoded string.  This is
	// used to verify good decryption and put in front of a new encrypted string
	private String crcHeader(String text) {
		CRC32 crc = new CRC32();
		crc.update(text.getBytes());
		int realCRC = (int)crc.getValue();
		
		// The first 4 chars of the hex string will equal the first
		// 4 chars of the decyphered text.  If they match we have a
		// good password.  This is what we return
		realCRC = realCRC ^ (-1);
		realCRC = realCRC >>> 0;
		String hexCRC = Integer.toHexString(realCRC).substring(0,4);
		return hexCRC.toString().toUpperCase();

	}

}
