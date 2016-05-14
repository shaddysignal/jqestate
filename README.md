# JQEstate country accamulator

Аккамулирует api.jqestate.ru/v1/properties/country при старте.
Затем раздает по пути /v1/properties, по страницам ограниченым параметрами pagination[limit] и pagination[offset].
Также можно использовать 2 фильтра:
* filter[id]
* filter[state]

Пример запроса /v1/properties?pagination[limit]=32&pagination[offset]=0&filter[id]=123,134&filter[state]=rented,public

При запуске стартует на порту 8080.
