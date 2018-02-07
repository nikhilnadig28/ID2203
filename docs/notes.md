## Notes

# Preliminary Report

- Initally plan was to use CHORD DHT. Probably too complex and time consuming. 
- Instead, use the template code to implement a simple key value pair and use range-based partitioning to distributed the data.
- Using range based partitoning to simplify the process. i.e. the assumption is to make input data to be only numbers, (say 0-1000) which would be simpler for testing purposes and to make partitioning simpler.
