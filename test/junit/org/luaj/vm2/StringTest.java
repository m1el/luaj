package org.luaj.vm2;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

import junit.framework.TestCase;

import org.luaj.vm2.lib.jse.JsePlatform;

public class StringTest extends TestCase {

	protected void setUp() throws Exception {
		JsePlatform.standardGlobals();
	}

	public void testToInputStream() throws IOException {
		LuaString str = LuaString.valueOf("Hello");
		
		InputStream is = str.toInputStream();
		
		assertEquals( 'H', is.read() );
		assertEquals( 'e', is.read() );
		assertEquals( 2, is.skip( 2 ) );
		assertEquals( 'o', is.read() );
		assertEquals( -1, is.read() );
		
		assertTrue( is.markSupported() );
		
		is.reset();
		
		assertEquals( 'H', is.read() );
		is.mark( 4 );
		
		assertEquals( 'e', is.read() );
		is.reset();
		assertEquals( 'e', is.read() );
		
		LuaString substr = str.substring( 1, 4 );
		assertEquals( 3, substr.length() );
		
		is.close();
		is = substr.toInputStream();
		
		assertEquals( 'e', is.read() );
		assertEquals( 'l', is.read() );
		assertEquals( 'l', is.read() );
		assertEquals( -1, is.read() );
		
		is = substr.toInputStream();
		is.reset();
		
		assertEquals( 'e', is.read() );
	}
	
	
	private static final String userFriendly( String s ) {
		StringBuffer sb = new StringBuffer();
		for ( int i=0, n=s.length(); i<n; i++ ) {
			int c = s.charAt(i);
			if ( c < ' ' || c >= 0x80 ) { 
				sb.append( "\\u"+Integer.toHexString(0x10000+c).substring(1) );
			} else {
				sb.append( (char) c );
			}
		}
		return sb.toString();
	}

	public void testUtf820482051() throws UnsupportedEncodingException {
		int i = 2048;
		char[] c = { (char) (i+0), (char) (i+1), (char) (i+2), (char) (i+3) };
		String before = new String(c)+" "+i+"-"+(i+4);
		LuaString ls = LuaString.valueOf(before);
		String after = ls.tojstring();
		assertEquals( userFriendly( before ), userFriendly( after ) );
		
	}
	
	public void testUtf8() {		
		for ( int i=4; i<0xffff; i+=4 ) {
			char[] c = { (char) (i+0), (char) (i+1), (char) (i+2), (char) (i+3) };
			String before = new String(c)+" "+i+"-"+(i+4);
			LuaString ls = LuaString.valueOf(before);
			String after = ls.tojstring();
			assertEquals( userFriendly( before ), userFriendly( after ) );
		}
		char[] c = { (char) (1), (char) (2), (char) (3) };
		String before = new String(c)+" 1-3";
		LuaString ls = LuaString.valueOf(before);
		String after = ls.tojstring();
		assertEquals( userFriendly( before ), userFriendly( after ) );
		
	}

	public void testSpotCheckUtf8() throws UnsupportedEncodingException {
		byte[] bytes = {(byte)194,(byte)160,(byte)194,(byte)161,(byte)194,(byte)162,(byte)194,(byte)163,(byte)194,(byte)164};
		String expected = new String(bytes, "UTF8");
		String actual = LuaString.valueOf(bytes).tojstring();
		char[] d = actual.toCharArray();
		assertEquals(160, d[0]);
		assertEquals(161, d[1]);
		assertEquals(162, d[2]);
		assertEquals(163, d[3]);
		assertEquals(164, d[4]);
		
	}
	
	public void testNullTerminated() {		
		char[] c = { 'a', 'b', 'c', '\0', 'd', 'e', 'f' };
		String before = new String(c);
		LuaString ls = LuaString.valueOf(before);
		String after = ls.tojstring();
		assertEquals( userFriendly( "abc\0def" ), userFriendly( after ) );
	}

	public void testIndexOfByteInSubstring() {
		LuaString str = LuaString.valueOf("abcdef:ghi");
		LuaString sub = str.substring(2, 10);
		assertEquals(10, str.m_length);
		assertEquals(8, sub.m_length);
		assertEquals(0, str.m_offset);
		assertEquals(2, sub.m_offset);
		
		assertEquals(6, str.indexOf((byte) ':', 0));
		assertEquals(6, str.indexOf((byte) ':', 2));
		assertEquals(6, str.indexOf((byte) ':', 6));
		assertEquals(-1, str.indexOf((byte) ':', 7));
		assertEquals(-1, str.indexOf((byte) ':', 9));
		assertEquals(9, str.indexOf((byte) 'i', 0));
		assertEquals(9, str.indexOf((byte) 'i', 2));
		assertEquals(9, str.indexOf((byte) 'i', 9));
		assertEquals(-1, str.indexOf((byte) 'z', 0));
		assertEquals(-1, str.indexOf((byte) 'z', 2));
		assertEquals(-1, str.indexOf((byte) 'z', 9));

		assertEquals(4, sub.indexOf((byte) ':', 0));
		assertEquals(4, sub.indexOf((byte) ':', 2));
		assertEquals(4, sub.indexOf((byte) ':', 4));
		assertEquals(-1, sub.indexOf((byte) ':', 5));
		assertEquals(-1, sub.indexOf((byte) ':', 7));
		assertEquals(7, sub.indexOf((byte) 'i', 0));
		assertEquals(7, sub.indexOf((byte) 'i', 2));
		assertEquals(7, sub.indexOf((byte) 'i', 7));
		assertEquals(-1, sub.indexOf((byte) 'z', 0));
		assertEquals(-1, sub.indexOf((byte) 'z', 2));
		assertEquals(-1, sub.indexOf((byte) 'z', 7));
	}

	public void testIndexOfPatternInSubstring() {
		LuaString str = LuaString.valueOf("abcdef:ghi");
		LuaString sub = str.substring(2, 10);
		assertEquals(10, str.m_length);
		assertEquals(8, sub.m_length);
		assertEquals(0, str.m_offset);
		assertEquals(2, sub.m_offset);

		LuaString pat = LuaString.valueOf(":");
		LuaString i = LuaString.valueOf("i");
		LuaString xyz = LuaString.valueOf("xyz");
		
		assertEquals(6, str.indexOf(pat, 0));
		assertEquals(6, str.indexOf(pat, 2));
		assertEquals(6, str.indexOf(pat, 6));
		assertEquals(-1, str.indexOf(pat, 7));
		assertEquals(-1, str.indexOf(pat, 9));
		assertEquals(9, str.indexOf(i, 0));
		assertEquals(9, str.indexOf(i, 2));
		assertEquals(9, str.indexOf(i, 9));
		assertEquals(-1, str.indexOf(xyz, 0));
		assertEquals(-1, str.indexOf(xyz, 2));
		assertEquals(-1, str.indexOf(xyz, 9));

		assertEquals(4, sub.indexOf(pat, 0));
		assertEquals(4, sub.indexOf(pat, 2));
		assertEquals(4, sub.indexOf(pat, 4));
		assertEquals(-1, sub.indexOf(pat, 5));
		assertEquals(-1, sub.indexOf(pat, 7));
		assertEquals(7, sub.indexOf(i, 0));
		assertEquals(7, sub.indexOf(i, 2));
		assertEquals(7, sub.indexOf(i, 7));
		assertEquals(-1, sub.indexOf(xyz, 0));
		assertEquals(-1, sub.indexOf(xyz, 2));
		assertEquals(-1, sub.indexOf(xyz, 7));
	}

	public void testLastIndexOfPatternInSubstring() {
		LuaString str = LuaString.valueOf("abcdef:ghi");
		LuaString sub = str.substring(2, 10);
		assertEquals(10, str.m_length);
		assertEquals(8, sub.m_length);
		assertEquals(0, str.m_offset);
		assertEquals(2, sub.m_offset);

		LuaString pat = LuaString.valueOf(":");
		LuaString i = LuaString.valueOf("i");
		LuaString xyz = LuaString.valueOf("xyz");
		
		assertEquals(6, str.lastIndexOf(pat));
		assertEquals(9, str.lastIndexOf(i));
		assertEquals(-1, str.lastIndexOf(xyz));

		assertEquals(4, sub.lastIndexOf(pat));
		assertEquals(7, sub.lastIndexOf(i));
		assertEquals(-1, sub.lastIndexOf(xyz));
	}

	public void testIndexOfAnyInSubstring() {
		LuaString str = LuaString.valueOf("abcdef:ghi");
		LuaString sub = str.substring(2, 10);
		assertEquals(10, str.m_length);
		assertEquals(8, sub.m_length);
		assertEquals(0, str.m_offset);
		assertEquals(2, sub.m_offset);

		LuaString ghi = LuaString.valueOf("ghi");
		LuaString ihg = LuaString.valueOf("ihg");
		LuaString ijk = LuaString.valueOf("ijk");
		LuaString kji= LuaString.valueOf("kji");
		LuaString xyz = LuaString.valueOf("xyz");
		LuaString ABCdEFGHIJKL = LuaString.valueOf("ABCdEFGHIJKL");
		LuaString EFGHIJKL = ABCdEFGHIJKL.substring(4, 12);
		LuaString CdEFGHIJ = ABCdEFGHIJKL.substring(2, 10);
		assertEquals(4, EFGHIJKL.m_offset);
		assertEquals(2, CdEFGHIJ.m_offset);
		
		assertEquals(7, str.indexOfAny(ghi));
		assertEquals(7, str.indexOfAny(ihg));
		assertEquals(9, str.indexOfAny(ijk));
		assertEquals(9, str.indexOfAny(kji));
		assertEquals(-1, str.indexOfAny(xyz));
		assertEquals(3, str.indexOfAny(CdEFGHIJ));
		assertEquals(-1, str.indexOfAny(EFGHIJKL));

		assertEquals(5, sub.indexOfAny(ghi));
		assertEquals(5, sub.indexOfAny(ihg));
		assertEquals(7, sub.indexOfAny(ijk));
		assertEquals(7, sub.indexOfAny(kji));
		assertEquals(-1, sub.indexOfAny(xyz));
		assertEquals(1, sub.indexOfAny(CdEFGHIJ));
		assertEquals(-1, sub.indexOfAny(EFGHIJKL));
	}
}
