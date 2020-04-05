from suds.client import Client
from suds.xsd.doctor import ImportDoctor, Import
from suds import sudsobject

url = "http://ws.webxml.com.cn/WebServices/WeatherWS.asmx?wsdl"

# Add a filter
# Reference:
# https://www.cnblogs.com/elephanyu/p/9136556.html
# https://stackoverflow.com/questions/4719854/soap-suds-and-the-dreaded-schema-type-not-found-error
imp = Import('http://www.w3.org/2001/XMLSchema',
             location='http://www.w3.org/2001/XMLSchema.xsd')
imp.filter.add('http://WebXml.com.cn/')
doctor = ImportDoctor(imp)
client = Client(url, doctor=doctor)

while True:
    print("Please input the city name.")
    city_name = input()
    result = client.service.getWeather(city_name)
    # Get rid of gif in results
    result_list = [i for i in sudsobject.asdict(result)["string"] if "gif" not in i]
    for i in result_list:
        print(i)

