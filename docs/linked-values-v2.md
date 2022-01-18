# LinkedValues v2

Новый механизм представлен
ограниченным [SPEL](https://docs.spring.io/spring-framework/docs/5.3.x/reference/html/core.html#expressions) в контексте
определения и вычисления выражений, заданных в конфигурации компонента (секция `linkedValues`).

Выражения, вычисленные в `linkedValues` (свойство `expression*`) записываются в секцию `arguments` по
пути `scenarioDto.display.arguments`


## 1. Общее описание

Объявление новой версии:

```json
{
  "linkedValues": [
    {
      "version": 2,
      "argument": "example",
      "definition": {
        "expression1": "arg1",
        "arg1": "'value'"
      }
    }
  ]
}
```

- `version` - версия `linkedValues` (2 - новая версия, 1 - старая версия, используется по умолчанию когда не указана версия);
- `argument` - ключ в `arguments`, под которым будет записан результат выражения;
- `definition` - блок выражения, состоящий из аргументов и выражений;
    - `expression1..10` - строка содержащая выражение, которое необходимо вычислить;
    - `arg1..10` - строка содержащая определение переменной, используемой в выражении.

Результатом выражения будет являться запись в `arguments` следующего вида:

```json
{
  "arguments": {
    "example": "value"
  }
}
```

### 1.1 Стандартные методы и константы
```java
//Методы приведения к типу
asString(Object)
asJson(Object)
asMap(String)
asList(String)
asDate(String)
asDate(String, String) //Второй параметр - формат даты первого аргумента 

asDateTime(String)
asDateTime(String, String) //Второй параметр - формат даты первого аргумента        
        
asInt(String)        
        
//Методы проверок массива
allMatch(String)
noneMatch(String)
anyMatch(String)
        
//Метод для выполнения нескольких expression друг за другом пока первый параметр не станет true 
getIf(boolean, Object)

//Константы
//строковая константа в arg1 и в expression1        
        "expression1": "arg1 + 'string_constanta'",
        "arg1": "'value'"

//int константа в arg1 и в expression1        
        "expression1": "arg1 + 123",
        "arg1": "100"

```


### 1.2 Ограничения и особенности работы 

```java
  Максимальное количество выражений expression: 10
  Максимальное количество переменных arg: 10
  
  В выражениях expression можно использовать только arg переменные, стандартные методы и константы
  В переменных arg можно использовать ссылки на поля, стандартные методы и константы
        
  Порядок вычисления:
    1. Расчет переменных arg       
    2. Расчет выражений expression
    3. Возврат результата    

  ВАЖНО! 
  - Все переменные arg расчитываются по порядку следования arg1, arg2, ... arg10
    - если во время расчета arg будет пустым, расчет будет остановлен 
  - Все выражения expression расчитываются по порядку следования epression1, epression2, ... epression10, если используется конструкция getIf()
    - если во время расчета expression будет пустым, расчет будет остановлен и возвращена как результат строка EMPTY_VALUE
```


## 2. Примеры

### 2.1 Ссылки

Для подстановки значений, присутствующих в `applicantAnswers`, в аргументы, применяется механизм разрешения ссылок

Данный механизм основывается на [JsonPath](https://github.com/json-path/JsonPath), что позволяет использовать все его
функциональные особенности, в том числе и фильтры.

Для проверки правильности jsonPath выражения, можно воспользоваться следующим ресурсом: https://jsonpath.com/

Ex. 2.1.1

applicantAnswers:

```json
{
  "applicantAnswers": {
    "pd1": {
      "visited": true,
      "value": "{\"key\":\"value\"}"
    }
  }
}
```

linkedValues:

```json
{
  "linkedValues": [
    {
      "version": 2,
      "argument": "example",
      "definition": {
        "expression1": "arg1",
        "arg1": "'${pd1.key}'"
      }
    }
  ]
}
```

result:

```json
{
  "arguments": {
    "example": "value"
  }
}
```

### 2.2 Фильтры

Стоит отметить, что результатом выражения фильтрации всегда будет **массив**.

Ex. 2.2.1

applicantAnswers:

```json
{
  "applicantAnswers": {
    "pd1": {
      "visited": true,
      "value": "{\"menu\":{\"id\":\"file\",\"value\":\"File\",\"popup\":{\"menuitem\":[{\"value\":\"New\",\"hidden\":false,\"onclick\":\"CreateNewDoc()\"},{\"value\":\"Open\",\"hidden\":false,\"onclick\":\"OpenDoc()\"},{\"value\":\"Close\",\"hidden\":false,\"onclick\":\"CloseDoc()\"}]}}}"
    }
  }
}
```

linkedValues:

```json
{
  "linkedValues": [
    {
      "version": 2,
      "argument": "example",
      "definition": {
        "expression1": "arg1",
        "arg1": "'${pd1.menu.popup.menuitem[?(@.value == 'New' && @.value == false)]}'"
      }
    }
  ]
}
```

result:

```json
{
  "arguments": {
    "example": "[{\"value\":\"New\",\"hidden\":false,\"onclick\":\"CreateNewDoc()\"}]"
  }
}
```

## 3 Приведение типов

Для приведения типов предусмотренны методы формата `as*` : `asString`, `asInt`, `asList` и др.

Ex. 3.1

linkedValues:

```json
{
  "linkedValues": [
    {
      "version": 2,
      "argument": "example",
      "definition": {
        "expression1": "arg1 == 501",
        "arg1": "asInt('000501')"
      }
    }
  ]
}
```

result:

```json
{
  "arguments": {
    "example": "true"
  }
}
```

## 4 Логические выражения и операторы сравнения

В качестве логических операторов используются стандартные операторы: `&&`, `||`, `==`, `!=`, а также тернарный
оператор `?:`

- прим. `"expression1": "(arg1 == arg2) ? 'equals' : 'not equals'`

Для сравнения используются стандартные операторы сравнения: `>`, `>=`, `<`, `<=`, `==`, `!=`

## 5 Конкатенация строк

Для конкатенации строк, в контексте строковых аргументов, используется оператор: `+`

Ex. 5.1

linkedValues:

```json
{
  "linkedValues": [
    {
      "version": 2,
      "argument": "example",
      "definition": {
        "expression1": "arg1 + ' ' + arg2 + ' ' + arg3",
        "arg1": "'Linked'",
        "arg2": "'Values'",
        "arg3": "'v2'"
      }
    }
  ]
}
```

result:

```json
{
  "arguments": {
    "example": "Linked Values v2"
  }
}
```

## 6 Методы

При использовании строк или результатов вызова методов, для них доступны специфичные для типа методы

- прим. `"expression1": "asString(asList(arg1).get(0))"` вызов метода `.get(index)` на объекте списка

Ex. 6.1

linkedValues:

```json
{
  "linkedValues": [
    {
      "version": 2,
      "argument": "example",
      "definition": {
        "expression1": "arg1.replaceAll('[-\\s]', '')",
        "arg1": "'000-000-000 00'"
      }
    }
  ]
}
```

result:

```json
{
  "arguments": {
    "example": "00000000000"
  }
}
```

В случае, если необходимо добавить специфичные методы, для использования в выражениях, следует реализовать эти методы в
классе `ru.gosuslugi.pgu.fs.common.service.functions.ExpressionMethods`.

Примером таких методов являются методы приведения типов и методы для работы с коллекциями: `allMatch(arg, key, value), anyMatch(arg, key, value), noneMatch(arg, key, value)` 

Ex. 6.2

applicantAnswers:

```json
{
  "applicantAnswers": {
    "pd1": {
      "visited": true,
      "value": "{\"id\":\"a8ed6bc8-4fb4-46da-affc-417b6dd93dcc\",\"persons\":[{\"fullName\":\"Mikella Cash\",\"sex\":\"FEMALE\",\"age\":23,\"status\":\"ACTIVE\"},{\"fullName\":\"Jurel Newell\",\"sex\":\"MALE\",\"age\":28,\"status\":\"ACTIVE\"},{\"fullName\":\"Elijio Woolley\",\"sex\":\"MALE\",\"age\":25,\"status\":\"ACTIVE\"}]}"
    }
  }
}
```

linkedValues:

```json
{
  "linkedValues": [
    {
      "version": 2,
      "argument": "example",
      "definition": {
        "expression1": "allMatch(arg1, 'status', 'ACTIVE') ? 'active' : 'non-active'",
        "arg1": "asList('${pd1.persons}')"
      }
    }
  ]
}
```

result:

```json
{
  "arguments": {
    "example": "active"
  }
}
```
## 7 Проверка и последовательное выполнение expressions getIf() 

```json
{
  "linkedValues": [
    {
      "version": 2,
      "argument": "example",
      "definition": {
        "expression1": "getIf('1000'.equals(arg1), 'municipal')",
        "expression2": "getIf('10000'.equals(arg1), 'district')",
        "expression3": "getIf('1000000'.equals(arg1), 'federal')",
        "expression4": "'region_level_undefined'",
        "arg1": "'${pd1.regionLevel}'"
      }
    }
  ]
}
```
