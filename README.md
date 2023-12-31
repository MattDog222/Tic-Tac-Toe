# Tic-Tac-Toe but in 7 Java Statements

The idea here is to make as fully functioning of a tic-tac-toe game as possible using as few of statements in Java as possible. This code is by no means meant to be readable or maintainable, but it is meant to be short and unironically efficient. I could make 1 line shorter by inlining a `(new java.util.Scanner(System.in)).nextLine()`, but that's _too_ inefficient for my taste (but it looks cooler).

### So how does it work?

The first 3 lines of code are unfortunately just going to be Java boilerplate (unless switching to a newer version of Java), which creates our `class`, `main`, and `Scanner` object. This `Scanner` makes up `50%` of the variables, but we don't need to go into detail on why we need it for a CLI tic-tac-toe game. Technically it is not needed, as mentioned in the intro.

The second variable, `int game` handles all the game data. This single variable controls the dataflow and manages the game state. This is because it is a bitmask.

## `int game`
This is a bitmask. In this readme we will refer to the least significant bit (LSB) as bit 0, and the most significant bit (MSB) as the leftmost bit (also known as 31).

### The 3x3 grid
The 3x3 grid of squares in tic-tac-toe are modeled here as 9 consecutive "cell-state" bitmasks.
These states are represented as 2-bit pairs as follows.

* `00` Available cell
* `01` Player 1's mark `x`
* `10` Player 2's mark `o`
* `11` N/A

_Note:_ Player 1 and Player 2's bitmasks have semantic meaning where their player number is the decimal equivalent (`01` for `1` and `10` for `2`). So commonly in the code the literals `1` and `2` refer to player 1 and player 2's bitmasks.

#### Cell numbering
The cells are numbered through the UI with values 1-9, but internally they are 0 based indices (0-8).
```
Game UI       Internally

1|2|3           0|1|2
-+-+-           -+-+-
4|5|6           3|4|5
-+-+-           -+-+-
7|8|9           6|7|8
```

These grid/cell states are managed with bits `0` through `17`. Bits 0-1 represent internal position 0 (top left), bits 2-3 represent internal position 1, ..., bits 16-17 represent internal position 8 (bottom right).

Suppose the grid was
```
x| |
-+-+-
 |o|
-+-+-
 |x|
```
then the last 18 bits would be `00_01_00_00_10_00_00_00_01`.

#### Updating State
We use `((game >>> 19) * 2)` to obtain the offset into the cell bitmasks (0, 2, 4, 6, 8, 10, 12, 14, or 16). See _User Inputs_ for what `game >>> 19` does, but note that we don't need to worry about ignoring the 3 most significant bits throwing off our shift because they have to be 0 (an error does not enter the `if`, and the end of the game doesnt re-enter the `while`). The multiplication by `2` is because the cell-bitmask has a width of `2`.

Next we check whose turn it is with `(game & 262144) == 0` (see _Player Turns_) to get the player's cell bitmask.  

Then we can take the player's bitmask and shift it over by the grid offset. This yeilds a bitmask with a single bit on, and because the input handling prevents re-use of an already used cell, the cell we're moving it into is already `00`. Thus we can `|=` or `^=` to set that bit to on. We choose to use `^=` because it allows us to simultaneously modify the player turn bit (see _Player Turns_).


#### Displaying
Most of the code is autogenerated in `CodeGenerator.java` under "Grid formats". This bitshifts `>>` by the grid position and checks if it is `0` (available), yields `' '`, otherwise checks if it is `1`, in which case it uses `'x'` for player 1, otherwise `'o'` for player 2. It does this for all 9 positions.


### Player Turns
Since there are only 2 players, and thus 2 states related to the turn, we only need 1 bit. For this, we used to next available bit after the cell states: bit `18`. This has the decimal value `262144`.
If the bit is on, `1`, then it is Player `1`'s turn. Otherwise, when it is off, `0`, it is Player 2's turn. This ordering helps maintain some semantic meaning to map the player number to the bit's value.

In order to check if it is player 1's turn, we can bitwise AND to check if the bits aligned or not. `(game & 262144) == 0` is true when it is player 2's turn. `(game & 262144) == 262144` is true when it is player 1's turn. Hence, `(game & 262144) != 0` is also true when it is player 1's turn. You can think of `(game & 262144) == 0` as the boolean variable `isPlayer2sTurn`, just inline evaluated each time to save such a declaration.

For example, we check it with `((game & 262144) == 0 ? 2 : 1)` to display `"Player 1"` or `"Player 2"` on the input prompt.


In order to toggle this bit we can XOR on that single bit, `262144`. We do this at the same time as setting the cell state (see _Updating State_). 

### User input
The input is stored in most of the remaining bits, `19` to `28`. Only integer values are supported, and they assume the UI 1-based indexing, so they are offset by `-1`. There is no error handling for non-integer values. There is some error handling for some invalid positions, but any bits higher than 10 are ignored. Thus, there are a lot of potential numbers, such as `-2147483647` or `268435459` with high order bits that get truncated, leaving `0` and `2` for these examples. But there are about 2^26 other "valid" numbers that act like this. As for errors within the 10 remaining bits, the only inputs accepted are 1-9 (which are then offset by 1), so anything valued higher than 8 is an error.

#### Error Bit
In the case of a invalid input error that we're able to handle, bit `29` is used to flag it. A `1` means there was an input error. This has the decimal value `536870912`. Checking this is like the Player Turn bit, we can use `(game & 536870912) == 0` to check for validity, or `(game & 536870912) != 0` for invalid inputs. Toggling can also be done with OR and XOR. 

When prompting for an input, there is a check on the error bit: `(game & 536870912) != 0`. If there is no error, we short circuit out of the full expression. If there is an error, we evaluate the right side of `&&`, which is `(game ^= 536870912) != 536870912`. This `^=` flips our bit back to being valid, and the `!= 536870912` ensures that this expression returns `true`, causing `Invalid!\n` to be prefixed.

When we check the validity of the input and detect that it is invalid (`false`) we do not short circuit out of `||` and instead hit `((game |= 536870912) == 0))` which toggles on the error bit and returns `false`, effectively `continue`-ing the while loop.

We read and check validity in the same compound expression. `(Integer.parseInt(in.nextLine()) - 1)` reads the input, parses it, and offsets it by `-1` for internal use. Then we `<< 22` to truncate the input down to the 10 input bits we can store. Then we right shift back `>>> 3` to put them in their correct positions in the bitmask. This entire setup is `(((Integer.parseInt(in.nextLine()) - 1) << 22) >>> 3)`

From here, we need to isolate the cell states and the player turn bits with `(game & 524287)`, where `524287` is a magic number of 19 least significant 1's. Then we can bitwise OR to store this input in the bits and we store that inline with `game = ((game & 524287) | (((Integer.parseInt(in.nextLine()) - 1) << 22) >>> 3))`.

Next we `>>> 19` to get the position into a semantic value, of which has to be positive since the MSB is 0. Hence, to check the domain [0, 8] we only need to compare `<= 8`
* `((game = ((game & 524287) | (((Integer.parseInt(in.nextLine()) - 1) << 22) >>> 3))) >>> 19) <= 8`

Our next validity check is that the space is available. This is done with `(game & (3 << ((game >>> 19) * 2))) == 0`. The magic number `3` is `0b11` to represent either player's cell position, we use `(game >>> 19)` for the desired cell position, multiply by `2` to get the scaled bitmask offset, and shift the `0b11` over to it. Here we bitwise AND, and as long as the result is `0` (no bits are set), the cell is available.

That ends the validity checks, resulting in the entire "isValid" check of  
`(((game = ((game & 524287) | (((Integer.parseInt(in.nextLine()) - 1) << 22) >>> 3))) >>> 19) <= 8 && (game & (3 << ((game >>> 19) * 2))) == 0)`

We abuse the logical OR short-circuit evaluation once again, wherein if the input is valid, we immediately enter the `if` statement. If it is invalid, we evaluate the right hand expression, `((game |= 536870912) == 0)`. This, as previously noted in _Error Bit_ will set an error, yield `false`, and not enter the `if`.




### Game Result / Ending
The state of the game is stored in the 2 most significant bits (`30` and `31`). Their meanings are as follows:

| Bits |    Meaning    |     Decimal |
|------|:-------------:|------------:|
| `00` |  In Progress  |           0 |
| `01` | Player 1 Wins |  1073741824 |
| `10` | Player 2 Wins | -2147483648 |
| `11` |      Tie      | -1073741824 |


Our `while` loop represents the game continuing while it is still in progress, `(game & -1073741824) == 0`.
After this condition fails and exits the loop, we print the result. Here we `>>> 30` to more easily compare `01`, `10,` and `11` (aka 1, 2, and 3). 3 is a tie, and 1 and 2 map to player 1 and player 2 winning respectively, so we can directly print `game >>> 30` if it wasn't a tie.

#### Updating state
At the very end of the while loop we see `game |= player1Wins ? 1073741824 : player2Wins ? -2147483648 : tied ? -1073741824 : 0`... well sort of.
There are a lot of logical ORs in there making it hard to see where `player1Wins`, `player2Wins`, and `tied` are at, but they are hardcoded values utilizing the bitmasks to check if each player won (row, column, diagonal), or if all cells where taken, or none of that (still in progress). These were programatically generated and concatenated.

Included is a `CodeGenerator.java` which prints out these comparisons after generating the magic numbers. This could be done in a very short manner because `01` and `10` have no intersections that would interfere with a bitwise AND. So if we want to check player 1 in a certain spot, simply ANDing `01` in each position and checking equality is enough. Same with `10`.

For the Tie game check it doesn't need to check for equality, just non-zero as the only time it is 0 is when the cell is unused:
* `01 & 11 == 01`
* `10 & 11 == 10`
* `00 & 11 == 00`
