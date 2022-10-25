
#Nullable columns in the Database
The academic meaning of "null" value is "unknown". Joins and conditions in a SQL are error prone while dealing with nullable columns.Nullable column is the last resort if the following alternatives fail to deliver the desired design goals

Use number zero, empty string or zero-date to indicate the value is to be ignored.

Use a three-valued enumeration instead of a nullable boolean.

Define another boolean valued column to indicate whether this column is known or not.

#Nullable arguments for methods
Default design is to expect non-null object values for all public methods. We will try to design over-loaded methods. In case we decide to allow a null value then the parameter is annotated as Nullable.

Any field that is not annotated is to be considered as non-null.

#NUllable returned object of a method
To be avoided for all public methods. Check if throwing an exception is better than allowing null. Optional<?> generic class must be used if it is appropriate to return null under some circumstance. This is to ensure that the API user is fully reminded to check for null before using the returned value.

Exception for this is a get operation from a name-value collection. Like Map. We find it natural to continue the 

 