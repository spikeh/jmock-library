package test.jmock;

public class ToDo {
    //Source cleanup prior to version 1.0 alpha
    //TODO RESOLVE ALL TO-DO COMMENTS
    //TODO JavaDoc interfaces and those classes that need it.
    //TODO Add copyright comments to files
    
	//-----------------------------------------------------------------------------------------
	// Tasks completed
	
	//DONE Implement matchers for call ordering.
	//DONE Add method to builder API for ordering calls across mocks
	//DONE Extract NoArgs and AnyArgs matcher classes from C
	//DONE Stop core packages relying on C
	//DONE Put low-level hook methods into builder interfaces
	//DONE Sensible default: If no expectation/match setup, invocation should produce no-op instead of fail.
	//  - this is to be dropped following user feedback
	//DONE When no args specified in dynamock.Mock, use ANY-args instead of NO-args.
	//  - dropped methods without arg specification in dynamock because of this ambiguity
    //DONE Expose setDefaultStub through high-level API
    //DONE Default result stub should return zero-length arrays for array types
    //DONE Make name of mock more visible in error messages
    //DONE ExpectNotCalled (or equivalent) makes tests more expressive.
    //DONE CoreMock should implement the hashCode method for proxies
    //DONE new hotmock syntax for expects and stubs 
    //DONE new hotmock syntax for expectNotCalled
    //DONE moved expect(...) and stub() to beginning of expectation statement
    //DONE demote dynamock API to an optional extension
    //DONE move default behaviour of default result stub into constructor
    //DONE reorganise directories. Change src/{framework,acceptance-tests} to core/{src,acceptance-tests}.
    //DONE open hooks to allow different proxying mechanisms to reuse CoreMock implementation
    //DONE Extract Describable interface. Priority one!
    //DONE Better format for expected calls in error messages
    //      - make expected and actual call signatures look similar
    //DONE Removed dynamock API. 
    //      - if anybody wants it they can pull it out of CVS and maintain it as an extension
    //DONE Test error message on verify failure
    //DONE Good names for high-level and low-level API packages
    //DONE Remove pending from BuilderId stuff. Complain if a matcher specifies an after clause for itself. 
    //DONE Remove deprecated methods
    //DONE make constraints SelfDescribing
    
	//-----------------------------------------------------------------------------------------
	// Post 1.0 tasks
	
    //TODO Clean up org.jmock.expecation package.
	//TODO Make jmock.expectation package use Constraints instead of calling equals on expected value
    //TODO Remove getters from Invocation (?)
    //TODO Extract interface from Invocation class (?)
    //TODO for trainer syntax: ((Thingy)mock.will(returnValue(10)).after("prev").id("asdad").train()),doIt(); ??
}