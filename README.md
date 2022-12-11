# 모든 맛집을 ZIP: MatZip
### MatZip?
맛집 리뷰를 올리고 공유하는 SNS 프로젝트입니다. 

### 배포
`main` 브랜치로 병합될 때 AWS Code Deploy로 자동적으로 배포가 이루어지고 있습니다. 실제 API들은 `http://matzip-server.shop` 로 테스팅이 
가능합니다.

### API 문서
API 문서는 Spring RestDocs 라이브러리를 활용하여 관리 합니다.
테스트를 거치면 API 문서가 생성됩니다. 
[API Docs](https://matzip-server.shop/docs/index.html)