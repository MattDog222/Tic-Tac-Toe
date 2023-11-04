import java.util.Scanner;
public class TicTacToeShort {
    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);
        int game = 0b1_00_00_00_00_00_00_00_00_00;
        while ((game & -1073741824) == 0) {
            System.out.println((((game & 536870912) != 0 && (game ^= 536870912) != 536870912) ? "Invalid!\n" : "") +  "Player " + ((game & 262144) == 0 ? 2 : 1) + " , Enter position (1-9) ");
            if ((((game = ((game & 524287) | (Integer.parseInt(in.nextLine()) - 1) << 19)) >>> 19) <= 8 && (game & (3 << ((game >>> 19) * 2))) == 0) || ((game |= 536870912) == 0)) {
                game ^= (((game & 262144) == 0 ? 2 : 1) << ((game >>> 19) * 2)) | 262144;
                System.out.printf("%s|%s|%s%n-+-+-%n%s|%s|%s%n-+-+-%n%s|%s|%s%n", ((game)&3)==0?' ':(((game)&1)==1?'x':'o'), ((game>>2)&3)==0?' ':(((game>>2)&1)==1?'x':'o'), ((game>>4)&3)==0?' ':(((game>>4)&1)==1?'x':'o'), ((game>>6)&3)==0?' ':(((game>>6)&1)==1?'x':'o'), ((game>>8)&3)==0?' ':(((game>>8)&1)==1?'x':'o'), ((game>>10)&3)==0?' ':(((game>>10)&1)==1?'x':'o'), ((game>>12)&3)==0?' ':(((game>>12)&1)==1?'x':'o'), ((game>>14)&3)==0?' ':(((game>>14)&1)==1?'x':'o'), ((game>>16)&3)==0?' ':(((game>>16)&1)==1?'x':'o'));
                game |= (game&21)==21 || (game&1344)==1344 || (game&86016)==86016 || (game&4161)==4161 || (game&16644)==16644 || (game&66576)==66576 || (game&65793)==65793 || (game&4368)==4368 ? 1073741824 : (game&42)==42 || (game&2688)==2688 || (game&172032)==172032 || (game&8322)==8322 || (game&33288)==33288 || (game&133152)==133152 || (game&131586)==131586 || (game&8736)==8736 ? -2147483648 : (game&3)!=0 && (game&12)!=0 && (game&48)!=0 && (game&192)!=0 && (game&768)!=0 && (game&3072)!=0 && (game&12288)!=0 && (game&49152)!=0 && (game&196608)!=0 ? -1073741824 : 0;
            }
        }
        System.out.println((game >>> 30) == 3 ? "Tie game!" : ("Player " + (game >>> 30) + " wins!"));
    }
}