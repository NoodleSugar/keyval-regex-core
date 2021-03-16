/**
 * <h1>Path regex (pregex)</h1>
 * <p>
 * A valid path regex is a sequence of elements delimited by 'point' characters; eg. <em>E<sub>1</sub>.E<sub>2</sub>. ... .E<sub>n</sub></em>.
 * An element E of a regex can contain a label, a value and a quantifier like: <code>"person=zuri{1,2}"</code>; each of its items can be absent, which can result in a full empty element.
 * An empty element is the one which syntactically has no label, and the meaning is that this element refer to the last defined node; it is used notably to refer to the root at the beginning of a sequence.
 * The disjunction
 * <q>{@code |}</q> create a new element that refer to the classic disjunction case, its priority is lower than the point
 * <q>{@code .}</q>.
 * Parenthesis can be used to define recursively an element containing more elements, and permits to change operators priority and to apply a quantifier.
 * Note that a parenthesis can immediately follows an element, in this case it is considered the same has if a point where present before the first parenthesis eg. <code>a(b) = a.b</code>.
 * </p>
 * <p>
 * A value or a label may be delimited by <em>delimiters</em>, which are pair of characters (begin/end) that can enclose them.
 * Delimiters can be set at the creation of the parser (see. {@link #PRegexParser(Map)}, {@link #PRegexParser(String)}).
 * When no delimiters are presents, the {@link Lexer} consider that a char belongs to a word if it verify {@code (c == '_' || Character.isLetterOrDigit(c) || Character.isSpaceChar(c))}.
 * </p>
 * <p>
 * As said before, an element without label represents an empty element that refer to the last defined node.
 * If a node is refered more than once than the last informations (reading from left to right) override the previous one.
 * For example <code>"a.=1.=2"</code> will set the value of <code>2</code> to the child node of the edge labeled with {@code a}.<br>
 * To specify explicitly a null label, the parser must set the <em>nullLabels</em> informations ({@link #setNullLabels(Collection)}).
 * <em>nullLabels</em> is a collection of {@link String} that stores labels that are considered to be null.
 * By default the parser consider the label
 * <q>_</q> to be a null label, so the regex:
 * <q>{@code _=1}</q> represents an edge labeled by a null followed by a node with the value of
 * <q>1</q> while
 * <q>=1</q> represents an empty element with a value.
 * </p>
 * <h3>Some examples:</h3>
 * <ul>
 * <li>person.name.(="zuri"|="noodle")</li>
 * <li>repeat=more{100}</li>
 * <li>=rootValue.element._=nullLabel</li>
 * </ul>
 * </p>
 * <h2>Rooted/terminal</h2>
 * <p>
 * An empty element may be prefixed by {@code ^} to refer to a true data root.
 * If {@code ^} has no relevant information to store on its own than it may be set to a labeled element with the meaning of a sequence beginning by an empty rooted element followed by a labeled element.
 * In other words, <code>"^label=val"</code> is equivalent to <code>"^.label=val"</code>.
 * An element may also be suffixed by {@code $} to refer to a true data leaf; it is called a terminal path.<br>
 * Rooted/Terminal qualifier may be in a repeated element, in this case only the first/last appearing element will be a root/terminal node.
 * <h2>Some examples:</h2>
 * <ul>
 * <li>^person</li>
 * <li>^=rootValue.person</li>
 * <li>name$</li>
 * <li>^.person.name=zuri$</li>
 * <li>(^t1|t2){2}$.=val</li>
 * <li>^=150$</li>
 * <li>^$</li>
 * </ul>
 * <h1>Tree regex (tregex)</h1>
 * <p>
 * A tree regex add the possibility to express some children on a node.
 * It can be done by separating path regexes with a comma
 * <q>,</q>; like <code>a(b,c)</code>.
 * If only one child must be present we can refer to an empty element in the node; like <code>(,child){4}</code>.
 * </p>
 * 
 * @author noodle
 * @author zuri
 */
package insomnia.implem.data.regex.parser;