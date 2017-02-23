///
/// Data Stack Level: L0, and L1
///
/// The simplest implmentation of picoded.dstack, which also serves as a reference implementation
///
/// While compliant, is not meant for very large scale persistent storage. As everything is kept in ram.
/// However it works well enough for L0 usage. Which is useful to save database calls, for inefficent application
/// implmentation that repeatedly calls for a DB value in multiple classes.
///
package picoded.dstack.struct.simple;